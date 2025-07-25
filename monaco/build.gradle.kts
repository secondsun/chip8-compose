val monacoBuilder : NamedDomainObjectProvider<Configuration> by configurations.registering {
    isCanBeConsumed = true
    isCanBeResolved = false
}

val zipTask = tasks.register<Zip>("build") {
    from("src")
    include("*")
    archiveFileName.set("monaco.zip")
    destinationDirectory.set(project.layout.buildDirectory)
}

artifacts {
    add(monacoBuilder.name, zipTask)
}