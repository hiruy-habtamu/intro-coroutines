package tasks

import contributors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {

    val repos =
        service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: emptyList()

    // Using mutableList here instead of making an immutable List is a bad idea in the case of concurrency
    // Things like race conditions are bound to happen as mutable functions such as MutableList<T>.add() ARE NOT ATOMIC!
    val users: MutableList<User> = emptyList<User>().toMutableList()

    repos.withIndex().forEach { (index, repo) ->
        val repoUsers = service
            .getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
        users.addAll(repoUsers)
        updateResults(users.aggregate(), index == repos.lastIndex)
    }
}
