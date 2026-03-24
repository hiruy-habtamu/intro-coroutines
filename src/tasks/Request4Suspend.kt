package tasks

import contributors.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {

    val repos =
        service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: emptyList()

    return repos.flatMap { repo ->
        service
            .getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
    }.aggregate()

}