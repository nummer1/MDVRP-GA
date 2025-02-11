import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.random.Random


class Population(val problem: Problem, var populationSize: Int) {

    var population: MutableList<Chromosome> = mutableListOf()

    init {
        for (i in 0.until(populationSize)) {
            population.add(Chromosome(problem))
        }
    }

    fun randomInitialization() {
        population.forEach { it.randomInitialization() }
    }

    fun bestInitialization() {
        val executor = Executors.newFixedThreadPool(8)
        for (i in population.indices) {
            val worker = Runnable {
                population[i].bestInitialization(Double.MAX_VALUE)
            }
            executor.execute(worker)
        }
        executor.shutdown()
        while (!executor.isTerminated) { }
    }

    fun closestInitialization() {
        val executor = Executors.newFixedThreadPool(8)
        for (i in population.indices) {
            val worker = Runnable {
                population[i].closestInitialization(Double.MAX_VALUE)
            }
            executor.execute(worker)
        }
        executor.shutdown()
        while (!executor.isTerminated) { }
    }

    fun initialization() {
        // half best and half random
        for (i in 0.until(population.size/3)) {
            population[i].randomInitialization()
        }
        val executor = Executors.newFixedThreadPool(8)
        for (i in (population.size/3).until(population.size/3*2)) {
            val worker = Runnable {
                population[i].bestInitialization(Double.MAX_VALUE, respectDuration = true)
            }
            executor.execute(worker)
        }
        for (j in (population.size/3*2).until(population.size)) {
            val worker = Runnable {
                population[j].closestInitialization(Double.MAX_VALUE, respectDuration = true)
            }
            executor.execute(worker)
        }
        executor.shutdown()
        while (!executor.isTerminated) { }

        // population.last().closestInitialization(Double.MAX_VALUE, respectDuration = true)

        for (chrome in population) {
            chrome.checkContainsEveryCustomer()
        }
    }

    fun createNewFitProp(crossoverRate: Double, mutationRate: Double, elitistCount: Int) {
        // create new population based on fitness proportionate ranking
        val popLock = ReentrantLock()
        val newPopulation: MutableList<Chromosome> = mutableListOf()
        var totalFitness = 0.0
        val maxFitness = getMaxFitness()
        val cumulativeFitness = List(population.size) { val fit = population[it].getFitness(maxFitness); totalFitness += fit; totalFitness }
        val fitness = MutableList(population.size) { population[it].getFitness(maxFitness) }

        // TODO: use cumulative fitness array and binary search
//        for (i in 0.until(populationSize/2)) {
//            var ind1_val = Random.nextDouble(0.0, totalFitness)
//            var ind2_val = Random.nextDouble(0.0, totalFitness)
//            cumulativeFitness.binarySearch { }
//        }

        // tournament selection
//        for (j in 0.until(populationSize/2 - elitistCount/2)) {
//
//        }

        val executor = Executors.newFixedThreadPool(8)

        // create new population
        // fitness based selection
        // TODO: check if elitistCount/2 crashes program when not even or 0
        for (j in 0.until(populationSize / 2 - elitistCount / 2)) {
            val worker = Runnable {
                val ind1Val = Random.nextDouble(0.0, totalFitness)
                val ind2Val = Random.nextDouble(0.0, totalFitness)
                val ind1_copy = Chromosome(problem)
                val ind2_copy = Chromosome(problem)
                var chosenInd1 = false
                var chosenInd2 = false
                for (i in 0.until(population.size)) {
                    if (ind1Val < cumulativeFitness[i] && !chosenInd1) {
                        ind1_copy.copyOf(population[i])
                        chosenInd1 = true
                    }
                    if (ind2Val < cumulativeFitness[i] && !chosenInd2) {
                        ind2_copy.copyOf(population[i])
                        chosenInd2 = true
                    }
                    if (chosenInd1 && chosenInd2) {
                        break
                    }
                }

                // do crossover
                if (Random.nextDouble(0.0, 1.0) < crossoverRate) {
                    if (!ind1_copy.crossoverRouteReassignment(ind2_copy, maxFitness)) {
                        ind1_copy.bestInitialization(maxFitness, respectDuration = Random.nextBoolean())
                        ind2_copy.closestInitialization(maxFitness, respectDuration = Random.nextBoolean())
                    }
                }

                // do mutation
                if (Random.nextDouble(0.0, 1.0) < mutationRate) {
                    ind1_copy.mutationSingleCustomerRerouting(maxFitness)
                }
                if (Random.nextDouble(0.0, 1.0) < mutationRate) {
                    ind2_copy.mutationSingleCustomerRerouting(maxFitness)
                }
                popLock.withLock { newPopulation.add(ind1_copy) }
                popLock.withLock { newPopulation.add(ind2_copy) }
            }
            executor.execute(worker)
        }

        executor.shutdown()
        while (!executor.isTerminated) { }

        // INFO: The below loop modifies population and fitness lists
        // copy elitistCount number of individuals to new population
        for (i in 0.until(elitistCount)) {
            val maxIdx = fitness.indices.maxBy { fitness[it] }
            val elite = population[maxIdx!!]
            newPopulation.add(elite)
            population.remove(elite)
            fitness.removeAt(maxIdx)
        }

        population = newPopulation
    }

    fun getMaxFitness(): Double {
        return population.maxBy { it.getCost() }!!.getCost() * 2
    }

    fun getFittest(): Chromosome {
        val maxFitness = getMaxFitness()
        return population.maxBy { it.getFitness(maxFitness) }!!
    }

    fun getAverageCost(): Double {
        var sum = 0.0
        population.forEach { sum += it.getCost() }
        return sum/population.size
    }

    fun printPop() {
        println("StartDepot, EndDepot, VehicleNumber, CustomerList")
        population.forEach { println(it); it.printChromosome() }
    }
}