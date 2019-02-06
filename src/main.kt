// TODO: check elitism
// https://towardsdatascience.com/introduction-to-genetic-algorithms-including-example-code-e396e98d8bf3
// https://pdfs.semanticscholar.org/c8ec/052d0266678e4deb20ac7af1014f9d7a27b4.pdf
// see wikipedia genetic algorithm and edge recombination

// https://www.mathworks.com/help/gads/how-the-genetic-algorithm-works.html


fun main(args: Array<String>) {
    val p01 = Problem("data_files/p20")
//    val s01 = Solution(p01)
//    s01.fromFile("solution_files/p01.res")
//    s01.toFile("test.txt")
//    s01.draw()

    val population = Population(p01, 1000)
    population.randomInitialization()
//    val s02 = Solution(p01)
//    s02.fromChromosome(population.population[0])
//    population.printPop()
//    s02.draw()
//    s02.toFile("test.txt")

    for (i in 0.until(1000)) {
        println(i)
        population.createNewFitProp(0.8, 0.1, 10)
    }

    population.printPop()
    val sol = Solution(p01)
    sol.fromChromosome(population.getFittest())
    sol.draw()
    sol.toFile("test.txt")
}