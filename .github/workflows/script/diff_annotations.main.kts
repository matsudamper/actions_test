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
    "git diff -U0",
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
                yield(diffResult.getOrNull(index + 2)?.startsWith("---") == true)
                yield(diffResult.getOrNull(index + 3)?.startsWith("+++") == true)
                yield(diffResult.getOrNull(index + 4)?.startsWith("@@") == true)
            }.all { it }
        ) {
            resultIndexList.add(index)
        }
    }
}

data class Annotation(
    val path: String,
    val start_line: Int,
    val end_line: Int,
    val annotation_level: String,
    val message: String,
)

val result = indexList.zip(indexList.drop(1).plus(diffResult.size)).map {
    diffResult.subList(it.first, it.second)
}.map {
    @Suppress("*")
    val filePath = it[2].drop("--- a/".length)
    val body = it.drop(4)
    System.err.println("body==============")
    System.err.println(body)
    val codeGroupIndex = buildList {
        body.mapIndexed { index, it ->
            val matchResult = """^@@ -(\d+),?(\d+)? \+(\d+),?(\d+)? @@""".toRegex().find(it)
            if (matchResult != null) {
                add(index)
            }
        }
    }
    codeGroupIndex.zip(codeGroupIndex.drop(1).plus(body.size)).map {
        body.subList(it.first, it.second)
    }.map {
        val matchResult = """^@@ -(\d+),?(\d+)? \+(\d+),?(\d+)? @@""".toRegex().find(it[0])!!
        val startIndex = matchResult.groups[1]!!.value.toInt()
        val count = matchResult.groups[2]?.value?.toInt() ?: 0
        Annotation(
            path = filePath,
            start_line = startIndex,
            end_line = startIndex + count,
            annotation_level = "failure",
            message = body.joinToString("\n"),
        )
    }
}

Gson().toJson(result.flatten())