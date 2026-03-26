package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.collections.flatten


suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {

    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    val users: List<Deferred<List<User>>> = repos.map { repo ->
        async {
            log("Starting loading  for ${repo.name}")
            delay(3000)
            service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
    users.awaitAll().flatten().aggregate()
}