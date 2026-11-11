package com.awarespan.guardian.ui.navigation

sealed class AwarespanNavDestination(val route: String) {
    data object Login : AwarespanNavDestination("login")
    data object Dashboard : AwarespanNavDestination("dashboard")
    data object IncidentFeed : AwarespanNavDestination("incidents")
    data object NodeList : AwarespanNavDestination("nodes")
    data object Map : AwarespanNavDestination("map")
    data object EventHistory : AwarespanNavDestination("events")
    data object Settings : AwarespanNavDestination("settings")

    data object IncidentDetail : AwarespanNavDestination("incidents/{incidentId}") {
        const val ARG_ID = "incidentId"
        fun createRoute(id: String) = "incidents/$id"
    }

    data object NodeDetail : AwarespanNavDestination("nodes/{nodeId}") {
        const val ARG_NODE_ID = "nodeId"
        fun createRoute(nodeId: String) = "nodes/$nodeId"
    }

    data object Autopilot : AwarespanNavDestination("autopilot?nodeId={nodeId}") {
        const val ARG_NODE_ID = "nodeId"
        fun createRoute(nodeId: String = "global") = "autopilot?nodeId=$nodeId"
    }
}

// Backwards-compatibility alias
typealias SipNavDestination = AwarespanNavDestination
