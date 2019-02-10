// TODO: add more Randomness to route crossover and mutation
// TODO: when valid solution found - save solution (if better than last valid solution)
// TODO: fix saving of files when exiting program
// https://towardsdatascience.com/introduction-to-genetic-algorithms-including-example-code-e396e98d8bf3
// https://pdfs.semanticscholar.org/c8ec/052d0266678e4deb20ac7af1014f9d7a27b4.pdf
// see wikipedia genetic algorithm and edge recombination

// https://www.mathworks.com/help/gads/how-the-genetic-algorithm-works.html


fun main(args: Array<String>) {
    val problem = Problem("data_files/p17")
    val ga = GA(problem)
    // ga.setVariables(popSize[j], 300, crossOverRate[j], 0.2)
    ga.run()
}