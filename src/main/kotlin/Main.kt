import java.net.URL
import java.util.*

fun getResource(path: String): URL {
    return object {}.javaClass.getResource(path)!!
}

data class User(
    val email: String,
    val name: String
)

val generatedIDs = hashSetOf<String>()

fun generateObjectID(): String {
    val date = Date().time / 1000
    val id = date.toString(16) + "xxxxxxxxxxxxxxxx".map {
        (Math.random() * 16).toInt().toString(16)
    }.joinToString("")
    assert(!generatedIDs.contains(id))
    return id
}

fun main() {
    val data = getResource("data.txt").readText().split("\n").map {
        val parts = it.split(":")
        val name = parts[1]
        var firstName = ""
        var lastName = ""
        var isAddingToFirstName = true
        var lastCharWasLowercase = false
        for (char in name) {
            if (isAddingToFirstName) {
                if (char.isUpperCase() && lastCharWasLowercase) {
                    isAddingToFirstName = false
                    lastName += char
                    continue
                }
                lastCharWasLowercase = char.isLowerCase()
                firstName += char
            } else {
                lastName += char
            }
        }
        User(parts[0], "$firstName $lastName")
    }.toMutableList()

    val oldEmails = getResource("old.txt").readText().split("\n").filter { it.isNotEmpty() }.toHashSet()
    val users = data.filterNot { oldEmails.contains(it.email) }
    println("Shoving ${users.size} out of ${data.size} users...")
    val commands = users.flatMap {
        val id = generateObjectID()
        listOf(
            "db.users.insertOne({_id: ObjectId(\"$id\"), name: \"${it.name}\", username: \"${it.email}\", password: \"2435rr01298450ierglmcdfxsdf234nv\", register_date: new Date()})",
            "db.sessions.insertOne({tutor: ObjectId(\"$id\")})"
        )
    }
    val lines = commands.chunked(20)
    for (line in lines) {
        println(line.joinToString(";"))
    }
}