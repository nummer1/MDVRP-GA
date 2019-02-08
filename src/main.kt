// TODO: add more Randomness to route crossover and mutation
// TODO: when valid solution found - save solution (if better than last valid solution)
// TODO: fix saving of files when exiting program
// https://towardsdatascience.com/introduction-to-genetic-algorithms-including-example-code-e396e98d8bf3
// https://pdfs.semanticscholar.org/c8ec/052d0266678e4deb20ac7af1014f9d7a27b4.pdf
// see wikipedia genetic algorithm and edge recombination

// https://www.mathworks.com/help/gads/how-the-genetic-algorithm-works.html


fun main(args: Array<String>) {
    val p01 = Problem("data_files/p15")
    val population = Population(p01, 400)
    population.randomInitialization()

    println("Average:cost: ${population.getAverageCost()}")
    println("Min:cost: ${population.getFittest().getCost()}")
    var i = 0
    while (true) {
        population.createNewFitProp(0.8, 0.2, 4)
        println(i)
        println("Average:cost: ${population.getAverageCost()}")
        val sol = Solution(p01)
        val fittest = population.getFittest()
        if (p01.depots[0].maxRouteDuration != 0) {
            if (fittest.fixChromosome()) {
                print("valid solution: ")
            }
        } else {
            print("valid solution: ")
        }
        println("Min:cost: ${fittest.getCost()}")
        sol.fromChromosome(population.getFittest())
        sol.draw("solution.png")
        sol.toFile("solution.txt")
        i ++
    }
}