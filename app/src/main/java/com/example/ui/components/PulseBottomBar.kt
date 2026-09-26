package com.example.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainNavigationTab
import com.example.ui.theme.PulseVioletPrimary

@Composable
fun PulseBottomBar(
    currentTab: MainNavigationTab,
    onTabSelected: (MainNavigationTab) -> Unit,
    unreadMessagesTotal: Int = 0,
    hasUnseenStatus: Boolean = false,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .testTag("pulse_bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // Chats
        NavigationBarItem(
            selected = currentTab == MainNavigationTab.CHATS,
            onClick = { onTabSelected(MainNavigationTab.CHATS) },
            modifier = Modifier.testTag("nav_tab_chats"),
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadMessagesTotal > 0) {
                            Badge(
                                containerColor = PulseVioletPrimary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(
                                    text = if (unreadMessagesTotal > 99) "99+" else "$unreadMessagesTotal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == MainNavigationTab.CHATS) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Chats"
                    )
                }
            },
            label = { Text("Chats") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        // Status
        NavigationBarItem(
            selected = currentTab == MainNavigationTab.STATUS,
            onClick = { onTabSelected(MainNavigationTab.STATUS) },
            modifier = Modifier.testTag("nav_tab_status"),
            icon = {
                BadgedBox(
                    badge = {
                        if (hasUnseenStatus) {
                            Badge(containerColor = PulseVioletPrimary)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == MainNavigationTab.STATUS) Icons.Filled.DonutLarge else Icons.Outlined.DonutLarge,
                        contentDescription = "Status"
                    )
                }
            },
            label = { Text("Status") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        // Channels
        NavigationBarItem(
            selected = currentTab == MainNavigationTab.CHANNELS,
            onClick = { onTabSelected(MainNavigationTab.CHANNELS) },
            modifier = Modifier.testTag("nav_tab_channels"),
            icon = {
                Icon(
                    imageVector = if (currentTab == MainNavigationTab.CHANNELS) Icons.Filled.Campaign else Icons.Outlined.Campaign,
                    contentDescription = "Channels"
                )
            },
            label = { Text("Channels") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        // Calls
        NavigationBarItem(
            selected = currentTab == MainNavigationTab.CALLS,
            onClick = { onTabSelected(MainNavigationTab.CALLS) },
            modifier = Modifier.testTag("nav_tab_calls"),
            icon = {
                Icon(
                    imageVector = if (currentTab == MainNavigationTab.CALLS) Icons.Filled.Phone else Icons.Outlined.Phone,
                    contentDescription = "Calls"
                )
            },
            label = { Text("Calls") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        // Settings
        NavigationBarItem(
            selected = currentTab == MainNavigationTab.SETTINGS,
            onClick = { onTabSelected(MainNavigationTab.SETTINGS) },
            modifier = Modifier.testTag("nav_tab_settings"),
            icon = {
                Icon(
                    imageVector = if (currentTab == MainNavigationTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
