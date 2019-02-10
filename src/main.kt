
fun main(args: Array<String>) {

    val problem = Problem("data_files/p01")
    val ga = GA(problem)

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            ga.shutdown()
        }
    })

    // ga.setVariables(popSize[j], 300, crossOverRate[j], 0.2)
    ga.run()
}