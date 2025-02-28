@file:OptIn(ExperimentalHorologistComposeLayoutApi::class, ExperimentalWearMaterialApi::class)

package com.omelan.cofi.wearos.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.omelan.cofi.share.Recipe
import com.omelan.cofi.share.RecipeIcon
import com.omelan.cofi.share.RecipeViewModel
import com.omelan.cofi.share.utils.getActivity
import com.omelan.cofi.wearos.R
import com.omelan.cofi.wearos.presentation.components.OpenOnPhoneConfirm
import com.omelan.cofi.wearos.presentation.components.RecipeListItem
import com.omelan.cofi.wearos.presentation.utils.WearUtils

@Composable
fun RecipeList(goToDetails: (Recipe) -> Unit, openSettings: () -> Unit) {
    val recipeViewModel: RecipeViewModel = viewModel()
    val recipes by recipeViewModel.getAllRecipes()
        .observeAsState(initial = listOf(null, null, null, null, null))
    RecipeList(recipes = recipes, goToDetails = goToDetails, openSettings = openSettings)
}

@Composable
fun RecipeList(
    recipes: List<Recipe?>,
    goToDetails: (Recipe) -> Unit = {},
    openSettings: () -> Unit = {},
) {
    var showOpenPhoneAppItem by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }
    val mainActivity = LocalContext.current.getActivity()
    WearUtils.ObserveIfPhoneAppInstalled { hasPhoneApp ->
        showOpenPhoneAppItem = !hasPhoneApp
    }
    LaunchedEffect(recipes) {
        if (recipes.isEmpty()) {
            showOpenPhoneAppItem = true
        }
    }
    val lazyListState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = lazyListState)
        },
        timeText = {
            TimeText(Modifier.scrollAway(lazyListState))
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .rotaryWithScroll(focusRequester, scrollableState = lazyListState)
                .background(MaterialTheme.colors.background),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Text(text = stringResource(id = R.string.app_name))
            }
            if (showOpenPhoneAppItem && mainActivity != null) {
                item {
                    Card(
                        onClick = {
                            WearUtils.openAppInStoreOnPhone(mainActivity) {
                                showConfirmation = true
                            }
                        },
                    ) {
                        Row {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.common_full_open_on_phone),
                                contentDescription = "",
                            )
                            Text(text = "Install Cofi on your phone")
                        }
                    }
                }
            }
            items(recipes) {
                RecipeListItem(recipe = it) {
                    if (it != null) goToDetails(it)
                }
            }
            item {
                Button(onClick = openSettings) {
                    Icon(
                        painterResource(id = R.drawable.round_settings_24),
                        contentDescription = "",
                    )
                }
            }
        }
        OpenOnPhoneConfirm(isVisible = showConfirmation, onTimeout = { showConfirmation = false })
    }

}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val recipes = listOf(
        Recipe(
            id = 1,
            name = stringResource(R.string.prepopulate_v60_name),
            description = stringResource(R.string.prepopulate_v60_description),
            recipeIcon = RecipeIcon.V60,
        ),
        Recipe(
            id = 2,
            name = stringResource(R.string.prepopulate_frenchPress_name),
            description = stringResource(R.string.prepopulate_frenchPress_description),
            recipeIcon = RecipeIcon.FrenchPress,
        ),
        Recipe(
            id = 3,
            name = stringResource(R.string.prepopulate_chemex_name),
            description = stringResource(R.string.prepopulate_chemex_description),
            recipeIcon = RecipeIcon.Chemex,
        ),
        Recipe(
            id = 4,
            name = stringResource(R.string.prepopulate_aero_name),
            description = stringResource(R.string.prepopulate_aero_description),
            recipeIcon = RecipeIcon.AeroPress,
        ),
    )
    RecipeList(recipes)
}
