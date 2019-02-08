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
        list.forEach { if (it.contains(customer)) println("contains1") }
        var assigned = false
        var breakLoop = false
        var size = 0
        list.forEach { size += it.size+1 }
        var placeIndex = Random.nextInt(0, size)
        for (i in 0.until(list.size)) {
            for (j in 0.until(list[i].size+1)) {
                if (placeIndex == 0) {
                    if (getVehicleLoad(i) + problem.customers[customer].quantityDemand < problem.maxVehicleLoad) {
                        list[i].add(j, customer)
                        assigned = true
                    }
                    breakLoop = true
                    break
                }
                placeIndex--
            }
            if (breakLoop) break
        }
        if (!assigned) {
            list.forEach { if (it.contains(customer)) println("contains2") }
            var listIndex = Random.nextInt(0, list.size)
            val startIndex = listIndex
            while (getVehicleLoad(listIndex) + problem.customers[customer].quantityDemand > problem.maxVehicleLoad) {
                if (listIndex == list.size - 1) listIndex = 0; else listIndex += 1
                if (startIndex==listIndex) println("failed Chromosome.randomAssignCustomer no routes with capacity")
            }
            list[listIndex].add(Random.nextInt(0, list[listIndex].size + 1), customer)
        }
    }

    private fun bestAssignCustomer(customer: Int): Boolean {
        // returns false if failed to insert customer
        // INFO: customer must be removed before function is called to get valid chromosome
        // add customer to best possible spot
        // list.forEach { it.remove(customer) }
        // list.forEach { if (it.contains(customer)) println("contains1") }
        val possibleList: MutableList<Pair<Int, Int>> = mutableListOf()
        val fitnessList: MutableList<Double> = mutableListOf()
        for (i in 0.until(list.size)) {
            for (j in 0.until(list[i].size + 1)) {
                if (getVehicleLoad(i) + problem.customers[customer].quantityDemand < problem.maxVehicleLoad) {
                    list[i].add(j, customer)
                    possibleList.add(Pair(i, j))
                    fitnessList.add(getFitness())
                    list[i].removeAt(j)
                }
            }
        }
        // list.forEach { if (it.contains(customer)) println("contains2") }
        if (possibleList.isEmpty()) {
            // println(problem.customers[customer].quantityDemand)
            return false
        } else {
            val maxIdx = fitnessList.indices.maxBy { fitnessList[it] }!!
            list[possibleList[maxIdx].first].add(possibleList[maxIdx].second, customer)
            return true
        }
    }

    fun fixChromosome(): Boolean {
        // returns true if fix successful
        var durSum = 0.0
        var maxSum = 0.0
        list.indices.forEach { maxSum += problem.depots[getStartDepot(it)].maxRouteDuration; durSum += getDuration(it) }
        if (durSum >= maxSum) {
            return false
        }
        val originalChromosome = Chromosome(problem)
        originalChromosome.copyOf(this)
        val reRouteCustomer = mutableListOf<Int>()
        for (k in 0.until(list.size)) {
            while (getDuration(k) > problem.depots[getStartDepot(k)].maxRouteDuration) {
                val randCustIndex = Random.nextInt(0, list[k].size)
                reRouteCustomer.add(list[k][randCustIndex])
                list[k].removeAt(randCustIndex)
            }
        }

        for (customer in reRouteCustomer) {
            val possibleList: MutableList<Pair<Int, Int>> = mutableListOf()
            val fitnessList: MutableList<Double> = mutableListOf()
            for (i in 0.until(list.size)) {
                for (j in 0.until(list[i].size + 1)) {
                    if (getVehicleLoad(i) + problem.customers[customer].quantityDemand < problem.maxVehicleLoad) {
                        list[i].add(j, customer)
                        if (getDuration(i) < problem.depots[getStartDepot(i)].maxRouteDuration) {
                            possibleList.add(Pair(i, j))
                            fitnessList.add(getFitness())
                        }
                        list[i].removeAt(j)
                    }
                }
            }
            // list.forEach { if (it.contains(customer)) println("contains2") }
            if (possibleList.isEmpty()) {
                // println(problem.customers[customer].quantityDemand)
                this.copyOf(originalChromosome)
                return false
            } else {
                val maxIdx = fitnessList.indices.maxBy { fitnessList[it] }!!
                list[possibleList[maxIdx].first].add(possibleList[maxIdx].second, customer)
            }
        }
        return true
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
        list.forEach { it.clear() }
        for (c in 0.until(problem.numberCustomers)) {
            var index = Random.nextInt(0, list.size)
            val startIndex = index
            while (problem.customers[c].quantityDemand + getVehicleLoad(index) > problem.maxVehicleLoad) {
                if (index == list.size-1) index = 0; else index += 1
                if (index == startIndex) {
                    println("Chromosome.randomInitialization failed, not room on any routes")
                    randomInitialization()
                    break
                }
            }
            list[index].add(c)
        }
    }

    fun copyOf(chromosome: Chromosome) {
        // makes this a copy of chromosome
        list.forEach { it.clear() }
        chromosome.list.forEachIndexed { index, l -> list[index].apply { addAll(l) } }
    }

    fun crossoverRouteReassignment(parent2: Chromosome): Boolean {
        // return false if failed
        // INFO: throws concurrent modification exception when called p1 == p2
        // take random route from p1 = r1
        // take random route from p2 = r2
        // remove customers in r1 from p2 and r2 from p1
        // assign customers from r1 to random routes in p2 and vice versa
        val r1 = this.list.random().toList()
        val r2 = parent2.list.random().toList()
        for (l in this.list) {
            r2.forEach { l.remove(it) }
        }
        for (l in parent2.list) {
            r1.forEach { l.remove(it) }
        }
        for (l in this.list) {
            r2.forEach { if (l.contains(it)) println("contains3") }
        }
        for (l in parent2.list) {
            r1.forEach { if (l.contains(it)) println("contains4") }
        }
//        p1.list.forEach { l -> r2.forEach { c -> l.remove(c) } }
//        p2.list.forEach { l -> r1.forEach { c -> l.remove(c) } }
        for (c in r2) {
            if (!this.bestAssignCustomer(c)) {
                return false
            }
        }
        for (c in r1) {
            if (!parent2.bestAssignCustomer(c)) {
                return false
            }
        }
        return true
    }

    fun mutationReverseSubroute() {
        // TODO
        // reverses a segment of a route
    }

    fun mutationSingleCustomerRerouting() {
        // randomly select customer - insert at best spot
        val c1 = Random.nextInt(0, problem.numberCustomers)
        list.forEach { it.remove(c1) }
        bestAssignCustomer(c1) // always returns true, since can insert at same place
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

    fun printChromosome() {
        for (index in 0.until(list.size)) {
            println("\t${getStartDepot(index)+1}, ${getClosestEndDepot(index)+1}, ${getVehicleNumber(index)+1}, ${list[index].map { it+1 }}")
        }
    }
}