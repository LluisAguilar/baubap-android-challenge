package com.baubap.challenge.ui.login.screens.state

sealed class MainFlowScreens {
    object login: MainFlowScreens()
    object register: MainFlowScreens()
    object home: MainFlowScreens()
}