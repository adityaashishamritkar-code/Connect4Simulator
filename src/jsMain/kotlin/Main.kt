import androidx.compose.runtime.*
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

fun main() {
    val config = GameConfig(rows = 7, cols = 6, winCondition = 4)
    val engine = ConnectFourEngine(config)

    engine.loadFromLocalStorage()

    renderComposable(rootElementId = "root") {
        var sliderRows by remember { mutableStateOf(engine.config.rows) }
        var sliderCols by remember { mutableStateOf(engine.config.cols) }
        var sliderWin by remember { mutableStateOf(engine.config.winCondition) }

        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Center)
                backgroundColor(Color.black)
                minHeight(100.vh)
                color(Color.white)
            }
        }) {
            H1 { Text("Connect ${engine.config.winCondition}") }

            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(40.px)
                    padding(20.px)
                    backgroundColor(rgba(255, 255, 255, 0.1))
                    borderRadius(12.px)
                    marginBottom(30.px)
                }
            }) {
                Label {
                    Div({ style { marginBottom(8.px) } }) { Text("Rows: $sliderRows") }
                    Input(InputType.Range) {
                        value(sliderRows.toString())
                        attr("min", "5")
                        attr("max", "20")
                        onInput { event ->
                            sliderRows = event.value?.toInt() ?: 7
                            engine.updateConfig(sliderRows, sliderCols, sliderWin)
                        }
                    }
                }

                Label {
                    Div({ style { marginBottom(8.px) } }) { Text("Cols: $sliderCols") }
                    Input(InputType.Range) {
                        value(sliderCols.toString())
                        attr("min", "4")
                        attr("max", "20")
                        onInput { event ->
                            sliderCols = event.value?.toInt() ?: 6
                            engine.updateConfig(sliderRows, sliderCols, sliderWin)
                        }
                    }
                }

                Label {
                    Div({ style { marginBottom(8.px) } }) { Text("Connect: $sliderWin") }
                    Input(InputType.Range) {
                        value(sliderWin.toString())
                        attr("min", "4")
                        attr("max", "10")
                        onInput { event ->
                            sliderWin = event.value?.toInt() ?: 4
                            engine.updateConfig(sliderRows, sliderCols, sliderWin)
                        }
                    }
                }
            }

            Div({
                style {
                    margin(15.px)
                    fontSize(1.8.em)
                    fontWeight("bold")
                    textAlign("center")
                    color(when {
                        engine.winner == Player.RED -> Color.red
                        engine.winner == Player.YELLOW -> Color.yellow
                        else -> Color.white
                    })
                }
            }) {
                val statusText = when {
                    engine.winner != null -> "${engine.winner} Wins!"
                    engine.isDraw -> "It's a Draw!"
                    else -> "${engine.currentPlayer}'s Turn"
                }
                Text(statusText)
            }

            key(engine.config.cols, engine.config.rows) {
                Div({
                    classes("game-board")
                    style {
                        display(DisplayStyle.Grid)
                        property("grid-auto-flow", "column")
                        gridTemplateColumns("repeat(${engine.config.cols}, 1fr)")
                        property("width", "min-content")
                        property("height", "auto")
                        padding(15.px)
                        property("margin", "0 auto")
                    }
                }) {
                    val gridSnapshot = engine.grid
                    repeat(engine.config.cols) { c ->
                        Div({
                            onClick { if (engine.winner == null && !engine.isDraw) engine.dropPiece(c) }
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                                width(50.px)
                                cursor(if (engine.winner == null) "pointer" else "default")
                            }
                        }) {
                            repeat(engine.config.rows) { r ->
                                val player = gridSnapshot.getOrNull(c)?.getOrNull(r)
                                Div({
                                    classes("piece", when(player) {
                                        Player.RED -> "piece-red"
                                        Player.YELLOW -> "piece-yellow"
                                        else -> "piece-empty"
                                    })
                                    style {
                                        margin(3.px)
                                        width(44.px)
                                        height(44.px)
                                    }
                                })
                            }
                        }
                    }
                }
            }

            Button({
                style {
                    marginTop(30.px)
                    padding(10.px, 20.px)
                    cursor("pointer")
                    fontSize(1.1.em)
                }
                onClick {
                    engine.reset()
                    window.localStorage.removeItem("c4_save")
                }
            }) {
                Text(if (engine.winner != null || engine.isDraw) "Play Again" else "Reset Game")
            }
        }
    }
}