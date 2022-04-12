#!/usr/bin/env kotlin
@file:CompilerOptions("-opt-in=kotlin.RequiresOptIn", "-nowarn")
@file:DependsOn("com.google.code.gson:gson:2.9.0")
@file:OptIn(ExperimentalStdlibApi::class)

import com.google.gson.Gson

val diffResult = args[0].split("\n")

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
        val matchResult = """^@@ -(\d+),?(\d+)? \+(\d+),?(\d+)? @@ (.+)$""".toRegex().find(it[0])!!
        val startIndex = matchResult.groups[1]!!.value.toInt()
        val count = matchResult.groups[2]?.value?.toInt() ?: 0
        Annotation(
            path = filePath,
            start_line = startIndex,
            end_line = startIndex + count,
            annotation_level = "failure",
            message = buildList {
                add(matchResult.groups[5]!!.value)
                addAll(it.drop(1))
            }.joinToString("\n"),
        )
    }
}

println(Gson().toJson(result.flatten()))
