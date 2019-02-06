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


class GA(problem: Problem) {

    var populationSize = 1000
    var generationNumber = 10000
    var crossoverRate = 0.8
    var mutationRate = 0.2
    var elitism = true
    val population = Population(problem, populationSize)
    val fitness = false

    init {
        population.randomInitialization()
    }

    fun runToConvergence() {
        // run until population has converged (minimal change in fitness)
    }

}