#!/usr/bin/env kotlin
@file:CompilerOptions("-opt-in=kotlin.RequiresOptIn", "-nowarn")
@file:DependsOn("com.google.code.gson:gson:2.9.0")
@file:OptIn(ExperimentalStdlibApi::class)

import java.io.File
import com.google.gson.Gson


val commentUrl = System.getenv("COMMENT_URL")
val githubToken = System.getenv("GITHUB_TOKEN")
val githubOwner = System.getenv("GITHUB_REPOSITORY_OWNER")
val githubServerUrl = System.getenv("GITHUB_SERVER_URL")
val githubRepository = System.getenv("GITHUB_REPOSITORY")


val currentBranchName = System.getenv("GITHUB_HEAD_REF")

val diffResult = Runtime.getRuntime().exec(
    "git diff",
    null,
    File(".")
).let { process ->
    process.errorStream.use { stream ->
        stream.bufferedReader().use { reader ->
            reader.lineSequence()
                .onEach { System.err.println(it) }
                .toList()
        }
    }

    process.inputStream.use { stream ->
        stream.bufferedReader().use { reader ->
            reader.lineSequence()
                .onEach { println(it) }
                .toList()
        }
    }
}

val indexList: List<Int> = buildList {
    val resultIndexList = this
    diffResult.mapIndexed { index, it ->
        if (
            sequence {
                yield(it.startsWith("diff --git"))
                yield(diffResult.getOrNull(index + 1)?.startsWith("index") == true)
                yield(diffResult.getOrNull(index + 2)?.startsWith("@@") == true)
            }.all { it }
        ) {
            resultIndexList.add(index)
        }
    }
}
indexList.zip(indexList.drop(1).plus(diffResult.size)).map {
    diffResult.subList(it.first, it.second)
}.forEach {
    println("========it========")
    println(it)
}

data class Annotation(
    val path: String
)

println(Gson().toJson(Annotation("")))