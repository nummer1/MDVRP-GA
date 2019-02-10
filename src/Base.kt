import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt


data class Customer(val customerNumber: Int, val xCoordinate: Int, val yCoordinate: Int, val serviceDuration: Double, val quantityDemand: Int)

data class Depot(val maxRouteDuration: Double, val maxVehicleLoad: Int, val xCoordinate: Int, val yCoordinate: Int)


class Route(var startDepot: Int, var endDepot: Int, val vehicleNumber: Int, val customerList: MutableList<Int>, val problem: Problem) {

    private fun getCoordinateList(): MutableList<Pair<Int, Int>> {
        val coordinateList: MutableList<Pair<Int, Int>> = mutableListOf()
        var depot = problem.depots[startDepot-1]
        coordinateList.add(Pair(depot.xCoordinate, depot.yCoordinate))
        for (c in customerList) {
            val customer = problem.customers[c-1]
            coordinateList.add(Pair(customer.xCoordinate, customer.yCoordinate))
        }
        depot = problem.depots[endDepot-1]
        coordinateList.add(Pair(depot.xCoordinate, depot.yCoordinate))
        return coordinateList
    }

    fun getDuration(): Double {
        var duration = 0.0
        for (c in customerList) {
            duration += problem.customers[c-1].serviceDuration
        }
        val coordinateList = getCoordinateList()
        var prevCoordinate = coordinateList[0]
        for (i in 1.until(coordinateList.size)) {
            val nextCoordinate = coordinateList[i]
            duration += sqrt(Math.pow(nextCoordinate.first.toDouble() - prevCoordinate.first.toDouble(), 2.0) +
                    Math.pow(nextCoordinate.second.toDouble() - prevCoordinate.second.toDouble(), 2.0))
            prevCoordinate = nextCoordinate
        }
        return duration
    }

    fun getCarriedLoad(): Int {
        var load = 0
        for (c in customerList) {
            load += problem.customers[c-1].quantityDemand
        }
        return load
    }
}


class Problem (fileName: String) {
    // read problem file and store necessary parameters

    val maxVehicles: Int  // max number of vehicles available in a depot
    val numberCustomers: Int
    val numberDepots: Int
    val maxVehicleLoad: Int
    val customers: MutableList<Customer> = mutableListOf()
    val depots: MutableList<Depot> = mutableListOf()

    init {
        val fileList: MutableList<String> = mutableListOf()
        File(fileName).useLines { lines -> fileList.addAll(lines) }
        val list: List<List<Int>> = fileList.map { x -> x.split(" ", "\t").filter { y -> y != ("")}.map { z -> z.toInt() } }

        maxVehicles = list[0][0]
        numberCustomers = list[0][1]
        numberDepots = list[0][2]

        // for each depot
        for (i in 1..numberDepots) {
            depots.add(Depot(list[i][0].toDouble(), list[i][1], list[i+numberCustomers+numberDepots][1], list[i+numberCustomers+numberDepots][2]))
        }
        // for each customer
        for (i in numberDepots+1..numberDepots+numberCustomers) {
            customers.add(Customer(list[i][0], list[i][1], list[i][2], list[i][3].toDouble(), list[i][4]))
        }

        maxVehicleLoad = depots[0].maxVehicleLoad
    }
}


class Solution(val problem: Problem) {

    var routes: MutableList<Route> = mutableListOf()

    fun fromChromosome(chromosome: Chromosome) {
        // Route(var startDepot: Int, var endDepot: Int, val vehicleNumber: Int, val customerList: MutableList<Int>, val problem: Problem)
        chromosome.list.forEachIndexed { index, l ->
            // println("${chromosome.getStartDepot(index)+1}, ${chromosome.getClosestEndDepot(index)+1}, ${chromosome.getVehicleNumber(index)+1}, $l")
            if (!l.isEmpty()) routes.add(Route(chromosome.getStartDepot(index)+1, chromosome.getClosestEndDepot(index)+1,
                chromosome.getVehicleNumber(index)+1, l.map { it+1 }.toMutableList(), problem))
        }
    }

    fun fromFile(fileName: String) {
        // read solution from file
        val fileList: MutableList<String> = mutableListOf()
        File(fileName).useLines { lines -> fileList.addAll(lines) }
        val list: List<List<String>> = fileList.map { x -> x.split(" ", "\t").filter { y -> y != ("")} }
        for (i in 1.until(list.size)) {
            val customerList: MutableList<Int> = mutableListOf()
            for (j in 5.until(list[i].size)) {
                customerList.add(list[i][j].toInt())
            }
            routes.add(Route(list[i][0].toInt(), list[i][4].toInt(), list[i][1].toInt(), customerList, problem))
        }
    }

    fun toFile(fileName: String) {
        // write solution to file
        var duration = 0.0
        for (route in routes) {
            duration += route.getDuration()
        }
        File(fileName).printWriter().use { out ->
            out.println("%.2f".format(duration))
            for (route in routes) {
                out.print("${route.startDepot}\t${route.vehicleNumber}\t${"%.2f".format(route.getDuration())}\t${route.getCarriedLoad()}\t${route.endDepot}\t")
                route.customerList.forEach { out.print("$it ") }
                out.println()
            }
        }
    }

    fun draw(fileName: String) {

        fun colorRectangle(_xStart: Int, _yStart: Int, _width: Int, _height: Int, hexColor: String, image: BufferedImage, useMiddle: Boolean=true) {
            var xStart = _xStart * 10
            var yStart = _yStart * 10
            val width = _width * 10
            val height = _height * 10
            if (useMiddle) {
                xStart -= (width/2)
                yStart -= (height/2)
            }
            for (x in xStart.until((xStart+width))) {
                for (y in yStart.until((yStart+height))) {
                    image.setRGB(x, y, Integer.parseInt(hexColor, 16))
                }
            }
        }

        fun drawLine(_xStart: Int, _xEnd: Int, _yStart: Int, _yEnd: Int, hexColor: String, image: BufferedImage) {
            var xStart = _xStart*10; var xEnd = _xEnd*10; var yStart = _yStart*10; var yEnd = _yEnd*10
            val intColor = Integer.parseInt(hexColor, 16)
            val w = xEnd - xStart
            val h = yEnd - yStart
            var dxStart = 0; var dyStart = 0; var dxEnd = 0; var dyEnd = 0
            if (w<0) dxStart = -1 ; else if (w>0) dxStart = 1
            if (h<0) dyStart = -1 ; else if (h>0) dyStart = 1
            if (w<0) dxEnd = -1 ; else if (w>0) dxEnd = 1
            var longest = Math.abs(w)
            var shortest = Math.abs(h)
            if (!(longest>shortest)) {
                longest = Math.abs(h)
                shortest = Math.abs(w)
                if (h<0) dyEnd = -1 ; else if (h>0) dyEnd = 1
                dxEnd = 0
            }
            var numerator = longest shr 1
            for (i in 0.until(longest+1)) {
                image.setRGB(xStart, yStart, intColor)
                numerator += shortest
                if (!(numerator < longest)) {
                    numerator -= longest
                    xStart += dxStart
                    yStart += dyStart
                } else {
                    xStart += dxEnd;
                    yStart += dyEnd
                }
            }
        }

        // draw solution
        val width: Int
        val height: Int
        var xMin = Int.MAX_VALUE
        var xMax = Int.MIN_VALUE
        var yMin = Int.MAX_VALUE
        var yMax = Int.MIN_VALUE
        val padding = 1

        for (d in problem.depots) {
            if (d.xCoordinate > xMax) xMax = d.xCoordinate
            else if (d.xCoordinate < xMin) xMin = d.xCoordinate
            if (d.yCoordinate > yMax) yMax = d.yCoordinate
            else if (d.yCoordinate < yMin) yMin = d.yCoordinate
        }
        for (c in problem.customers) {
            if (c.xCoordinate > xMax) xMax = c.xCoordinate
            else if (c.xCoordinate < xMin) xMin = c.xCoordinate
            if (c.yCoordinate > yMax) yMax = c.yCoordinate
            else if (c.yCoordinate < yMin) yMin = c.yCoordinate
        }
        width = xMax - xMin
        height = yMax - yMin

        val image = BufferedImage((width*10)+(padding*20), (height*10)+(padding*20), BufferedImage.TYPE_INT_RGB)
        // make background light grey
        colorRectangle(0, 0, width+(padding*2), height+(padding*2), "C0C0C0", image, useMiddle=false)

        // draw customers and lines in different color for each depot
        val colorList = mutableListOf("FF0000", "00FF00", "0000FF", "FFFF00", "FF00FF", "800000",
            "008000", "000080", "808000", "800080", "008080", "808080", "C00000", "00C000", "0000C0", "C0C000",
            "C000C0", "00C0C0", "400000", "004000", "000040", "404000", "400040", "004040", "404040",
            "200000", "002000", "000020", "202000", "200020", "002020", "202020", "600000", "006000", "000060",
            "606000", "600060", "006060", "606060", "A00000", "00A000", "0000A0", "A0A000", "A000A0", "00A0A0",
            "A0A0A0", "E00000", "00E000", "0000E0", "E0E000", "E000E0", "00E0E0", "E0E0E0")
        // colorList.shuffle()

        for (r in routes) {
            val color = colorList[r.startDepot-1]
            var xDepot = problem.depots[r.startDepot-1].xCoordinate-xMin
            var yDepot = problem.depots[r.startDepot-1].yCoordinate-yMin
            var startCustomer = problem.customers[r.customerList.first()-1]
            var endCustomer = problem.customers[r.customerList.first()-1]
            drawLine(xDepot+padding, startCustomer.xCoordinate-xMin+padding, yDepot+padding, startCustomer.yCoordinate-yMin+padding, color, image)
            for (c in 1.until(r.customerList.size)) {
                colorRectangle(startCustomer.xCoordinate-xMin+padding, startCustomer.yCoordinate-yMin+padding, 1, 1, color, image)
                endCustomer = problem.customers[r.customerList[c]-1]
                drawLine(startCustomer.xCoordinate-xMin+padding, endCustomer.xCoordinate-xMin+padding, startCustomer.yCoordinate-yMin+padding, endCustomer.yCoordinate-yMin+padding, color, image)
                startCustomer = endCustomer
            }
            colorRectangle(startCustomer.xCoordinate-xMin+padding, startCustomer.yCoordinate-yMin+padding, 1, 1, color, image)
            xDepot = problem.depots[r.endDepot-1].xCoordinate-xMin
            yDepot = problem.depots[r.endDepot-1].yCoordinate-yMin
            drawLine(endCustomer.xCoordinate-xMin+padding, xDepot+padding, endCustomer.yCoordinate-yMin+padding, yDepot+padding, color, image)
        }

        // draw depots with light blue
        for (d in problem.depots) {
            colorRectangle(d.xCoordinate-xMin+padding, d.yCoordinate-yMin+padding, 1, 1, "00FFFF", image)
        }

        val outFile = File(fileName)
        ImageIO.write(image, "png", outFile)
    }
}
