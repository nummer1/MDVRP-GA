import kotlin.math.max
import kotlin.math.min
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

    fun createNewFitProp(crossoverRate: Double, mutationRate: Double, elitistCount: Int) {
        // create new population based on fitness proportionate ranking
        val newPopulation: MutableList<Chromosome> = mutableListOf()
        var totalFitness = 0.0
        val cumulativeFitness = List(populationSize) { val fit = population[it].getFitness(); totalFitness += fit; totalFitness }
        val fitness = MutableList(population.size) { population[it].getFitness() }

        // TODO: use cumulative fitness array and binary search
//        for (i in 0.until(populationSize/2)) {
//            var ind1_val = Random.nextDouble(0.0, totalFitness)
//            var ind2_val = Random.nextDouble(0.0, totalFitness)
//            cumulativeFitness.binarySearch { }
//        }

        // create new population
        // TODO: check if elitistCount/2 crashes program when not even
        for (j in 0.until(populationSize/2 - elitistCount/2)) {
            val ind1Val = Random.nextDouble(0.0, totalFitness)
            val ind2Val = Random.nextDouble(0.0, totalFitness)
            var ind1 = Chromosome(problem)
            var ind2 = Chromosome(problem)
            var chosenInd1 = false
            var chosenInd2 = false
            for (i in 0.until(population.size)) {
                if (ind1Val < cumulativeFitness[i] && !chosenInd1) {
                    ind1.copyOf(population[i])
                    chosenInd1 = true
                }
                if (ind2Val < cumulativeFitness[i] && !chosenInd2) {
                    ind2.copyOf(population[i])
                    chosenInd2 = true
                }
                if (chosenInd1 && chosenInd2) {
                    break
                }
            }
            while (ind1 == ind2) {
                ind2 = population.random()
            }

            // do crossover
            if (Random.nextDouble(0.0, 1.0) < crossoverRate) {
                ind1.crossoverRouteReassignment(ind1, ind2)
            }

            // do mutation
            if (Random.nextDouble(0.0, 1.0) < mutationRate) {
                ind1.mutationSingleCustomerRerouting()
            }
            if (Random.nextDouble(0.0, 1.0) < mutationRate) {
                ind2.mutationSingleCustomerRerouting()
            }

            newPopulation.add(ind1)
            newPopulation.add(ind2)
        }

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

    fun getFittest(): Chromosome {
        return population.maxBy { it.getFitness() }!!
    }

    fun printPop() {
        println("StartDepot, EndDepot, VehicleNumber, CustomerList")
        population.forEach { println(it); it.printChromosone() }
    }
}