import kotlin.math.sqrt
import kotlin.random.Random


class Chromosome(private val problem: Problem) {
    // REMEMBER: ZERO INDEXED

    val list: List<MutableList<Int>> = List(problem.numberDepots * problem.maxVehicles) { mutableListOf<Int>() }

    private fun getDistance(x1: Int, x2: Int, y1: Int, y2: Int): Double {
        // return distance between two points
        return sqrt(Math.pow(x1.toDouble() - x2.toDouble(), 2.0) +
                Math.pow(y1.toDouble() - y2.toDouble(), 2.0))
    }

    private fun getCoordinatesSingleList(index: Int): MutableList<Pair<Int, Int>> {
        val coordinateList: MutableList<Pair<Int, Int>> = mutableListOf()
        var depot = problem.depots[getStartDepot(index)]
        coordinateList.add(Pair(depot.xCoordinate, depot.yCoordinate))
        for (c in list[index]) {
            val customer = problem.customers[c]
            coordinateList.add(Pair(customer.xCoordinate, customer.yCoordinate))
        }
        depot = problem.depots[getClosestEndDepot(index)]
        coordinateList.add(Pair(depot.xCoordinate, depot.yCoordinate))
        return coordinateList
    }

    private fun getDuration(index: Int): Double {
        // returns duration of single route
        var duration = 0.0
        for (c in list[index]) {
            duration += problem.customers[c].serviceDuration
        }
        val coordinateList = getCoordinatesSingleList(index)
        var prevCoordinate = coordinateList[0]
        for (i in 1.until(coordinateList.size)) {
            val nextCoordinate = coordinateList[i]
            duration += getDistance(nextCoordinate.first, prevCoordinate.first, nextCoordinate.second, prevCoordinate.second)
            prevCoordinate = nextCoordinate
        }
        return duration
    }

    private fun getVehicleLoad(index: Int): Int {
        // return load of index: index
        var load = 0
        list[index].forEach { c -> load += problem.customers[c].quantityDemand }
        return load
    }

    private fun randomAssignCustomer(customer: Int) {
        // INFO: function never stalls, since can always assign customer to same route
        // remove customer from current route and randomly assign to other route
        list.forEach { it.remove(customer) }
        var listIndex = Random.nextInt(0, list.size)
        while (getVehicleLoad(listIndex) + problem.customers[customer].quantityDemand > problem.maxVehicleLoad) {
            if (listIndex == list.size-1) listIndex = 0; else listIndex += 1
        }
        list[listIndex].add(Random.nextInt(0, list[listIndex].size+1), customer)
    }

    fun getClosestEndDepot(index: Int): Int {
        // returns closest depot to end customer, zero-indexed
        // if list is empty, return same depot
        if (list[index].isEmpty()) {
            return getStartDepot(index)
        }
        var distance = Double.MAX_VALUE
        var closest = -1
        val c = problem.customers[list[index].last()]
        for (d in 0.until(problem.numberDepots)) {
            var newDistance = getDistance(problem.depots[d].xCoordinate, c.xCoordinate, problem.depots[d].yCoordinate, c.yCoordinate)
            if (newDistance < distance) {
                distance = newDistance
                closest = d
            }
        }
        return closest
    }

    fun getStartDepot(index: Int): Int {
        // returns start depot, zero-indexed
        return index/problem.maxVehicles
    }

    fun getVehicleNumber(index: Int): Int {
        // returns vehicle number, zero-indexed
        return index%problem.maxVehicles
    }

    fun randomInitialization() {
        for (c in 0.until(problem.numberCustomers)) {
            var index = Random.nextInt(0, list.size)
            while (problem.customers[c].quantityDemand + getVehicleLoad(index) > problem.maxVehicleLoad) {
                // TODO: if no route has capacity, this will go on indefinitely
                if (index == list.size-1) index = 0; else index += 1
            }
            list[index].add(c)
        }
    }

    fun copyOf(chromosome: Chromosome) {
        chromosome.list.forEachIndexed { index, l -> list[index].apply { addAll(l) } }
    }

    fun crossoverRouteReassignment(p1: Chromosome, p2: Chromosome) {
        // INFO: throws cncurrent modification exception when called p1 == p2
        // TODO: best spot
        // take random route from p1 = r1
        // take random route from p2 = r2
        // remove customers in r1 from p2 and r2 from p1
        // assign customers from r1 to random routes in p2 and vice versa
        val r1 = p1.list.random()
        val r2 = p2.list.random()
        r1.forEach { p2.randomAssignCustomer(it) }
        r2.forEach { p1.randomAssignCustomer(it) }
    }

    fun mutationReverseSubroute() {
        // TODO
        // reverses a segment of a route
    }

    fun mutationSingleCustomerRerouting() {
        // TODO: best spot
        // randomly select customer - insert at best spot
        val c1 = Random.nextInt(0, problem.numberCustomers)
        randomAssignCustomer(c1)
    }

    fun mutationSwapCustomers() {
        // TODO
        // swaps two random customers between two random routes
    }

    fun getCost(): Double {
        // returns cost of whole solution
        var cost = 0.0
        for (i in list.indices) {
            cost += getDuration(i)
        }
        return cost
    }

    fun getFitness(): Double {
        return 1/getCost()
    }

    fun printChromosone() {
        for (index in 0.until(list.size)) {
            println("\t${getStartDepot(index)+1}, ${getClosestEndDepot(index)+1}, ${getVehicleNumber(index)+1}, ${list[index].map { it+1 }}")
        }
    }
}