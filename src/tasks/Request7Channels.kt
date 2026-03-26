package tasks

import contributors.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService, req: RequestData, updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service.getOrgRepos(req.org).also { logRepos(req, it) }.body() ?: emptyList()

        val channel = Channel<List<User>>()

        repos.forEach { repo ->
            launch {
                val repoUsers =
                    service.getRepoContributors(req.org, repo.name)
                        .also { logUsers(repo, it) }
                        .bodyList()

                channel.send(repoUsers)
            }
        }
        var allUsers = emptyList<User>()
        launch {
            repeat(repos.size) {
                val users = channel.receive()
                allUsers = (allUsers + users).aggregate()
                updateResults(allUsers, it == repos.lastIndex)
            }
        }

    }
}
