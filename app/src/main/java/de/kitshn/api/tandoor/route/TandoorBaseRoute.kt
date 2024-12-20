package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient

abstract class TandoorBaseRoute(
    open val client: TandoorClient
)