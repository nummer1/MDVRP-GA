import java.io.File

//START
//Generate the initial population
//Compute fitness
//REPEAT
//Selection
//Crossover
//Mutation
//Compute fitness
//UNTIL population has converged
//STOP


class GA(val problem: Problem) {

    var populationSize = 400
    var generationNumber = Int.MAX_VALUE
    var crossoverRate = 0.9
    var mutationRate = 0.2
    var elitistCount = 10

    fun setVariables(populationSize: Int, generationsNumber: Int, crossoverRate: Double, mutationRate: Double) {
        this.populationSize = populationSize
        this.generationNumber = generationsNumber
        this.crossoverRate = crossoverRate
        this.mutationRate = mutationRate
    }

    fun run() {
        val population = Population(problem, populationSize)
        var bestValidSolution = Double.MAX_VALUE
        population.initialization()

        println("Average:cost: ${population.getAverageCost()}")
        println("BestFit:cost: ${population.getFittest().getCost()}")
        for (i in 0.until(generationNumber)) {
            population.createNewFitProp(crossoverRate, mutationRate, elitistCount)
            println(i)
            println("Average:cost: ${population.getAverageCost()}")
            val sol = Solution(problem)
            var fittest = population.getFittest()
            if (problem.depots[0].maxRouteDuration != 0.0) {
                val fittestCopy = Chromosome(problem)
                fittestCopy.copyOf(fittest)
                if (fittestCopy.fixChromosome()) {
                    fittest = fittestCopy
                    population.population.add(fittest)
                    print("valid solution: ")
                    if (fittest.getCost() < bestValidSolution) {
                        //                    for (j in fittestCopy.list.indices) {
                        //                        if (fittestCopy.getDuration(j) > 310.0) println(fittestCopy.getDuration(j))
                        //                    }
                        bestValidSolution = fittest.getCost()
                        sol.fromChromosome(fittest)
                        sol.toFile("valid_solution.txt")
                        sol.draw("valid_solution.png")
                    }
                }
            } else {
                print("valid solution: ")
                if (fittest.getCost() < bestValidSolution) {
                    bestValidSolution = fittest.getCost()
                    sol.fromChromosome(fittest)
                    sol.toFile("valid_solution.txt")
                    sol.draw("valid_solution.png")
                }
            }
            println("BestFit:cost: ${fittest.getCost()}")
            sol.fromChromosome(fittest)
            sol.draw("solution.png")
            sol.toFile("solution.txt")
        }
    }
}