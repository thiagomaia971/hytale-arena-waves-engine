package com.miilhozinho.arenawavesengine.hud

import au.ellie.hyui.html.TemplateProcessor
import com.google.gson.Gson
import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.util.LogUtil

class ActiveSessionHud{

    companion object {

        fun createTemplateProcessor(session: ArenaSession, playerId: String): TemplateProcessor {
            val template = TemplateProcessor()
                .registerComponent("enemyMob", enemyMobComponent)

            val mapDef = ArenaWavesEngine.repository.get().arenaMaps.find { it.id == session.waveMapId } ?: return template
            val currentWaveDef = mapDef.waves[session.currentWave] ?: return template
            val currentWaveData = session.wavesData[session.currentWave] ?: return template

            val enemiesKilled = currentWaveData.enemies.values.sumOf {it.killed}
            val totalEnemiesWave = currentWaveData.enemies.values.sumOf {it.getTotalEnemiesWave()}
            val progressBarValue = enemiesKilled.toFloat() / totalEnemiesWave.toFloat()
            val enemiesList = currentWaveData.enemies.values.toList()

            val totalIntervalWait = currentWaveDef.interval * 1000L
            val intervalElapsed = session.getElapsedIntervalTime()
            val percentage = (intervalElapsed.toFloat() / totalIntervalWait.toFloat())
            val intervalElapsedLeft = session.getElapsedIntervalLeftTime(mapDef)

            LogUtil.debug("Drawing ${Gson().toJson(session)}")

            template.setVariable("mapName", session.waveMapId)
            template.setVariable("waveState", session.state)
            template.setVariable("waveCount", "${session.currentWave + 1} / ${mapDef.waves.count()}")
            template.setVariable("enemiesRemain", "${(totalEnemiesWave - enemiesKilled)} / $totalEnemiesWave")
            template.setVariable("waveProgressBar", progressBarValue)
            template.setVariable("waveProgressLabel", "${(progressBarValue * 100).toInt()}%")
            template.setVariable("playerScore", session.getDamageScore(playerId))
            template.setVariable("enemiesData", enemiesList)
            template.setVariable("elapsedTime", session.getElapsedTime())
            template.setVariable("intervalLeftProgress", percentage)
            template.setVariable("intervalLeft", intervalElapsedLeft)
//        }

            return template;
        }

        val html = """
            <style>
                .active-session-hud {
                    anchor-width: 300;
                    anchor-height: 400;
                    anchor-right: 10;
                    anchor-top: 40;
                    background-image: url("Hud/ArenaWavesEngine/ObjectivePanelContainer.png") 20;
                }

                .active-session-hud-container {
                    layout-mode: Top;
                }

                .row-display {
                    text-align: center;
                    anchor-height: 40;
                }

                .row-display-wave-progress {
                    background-image: url("enemies-remain-background.png") 0;
                    anchor-height: 55;
                    anchor-top: 0;
                }

                .center-label {
                    vertical-align: center;
                    horizontal-align: center;
                }

                .wave-progress-label {
                    text-align: center;
                    anchor-top: 0;
                }

                .wave-progress-bar {
                    text-align: center;
                //anchor-top: 30;
                }
                .row-display-label {
                    //anchor-left: 50;
                    //anchor-right: 15;
                    text-transform: uppercase;
                    font-size: 18;
                    font-weight: bold;
                    color: #FFFFFF;
                }

                .row-display-value {
                    text-transform: uppercase;
                    font-size: 18;
                    font-weight: bold;
                    text-color: #FFFFFF;
                }


                .state-display {
                    text-align: center;
                }

                .state-display-style {
                    background-color: #FFAD42(0.4);
                    anchor-width: 300;
                    anchor-height: 25;
                    text-align: center;
                    vertical-align: center;
                }

                .enemy-list {
                    flex-direction: column;
                }

                .mob-row {
                    flex-direction: row;
                    anchor-left: 20;
                }

                .mob-text {
                    color: #D1E8FF;
                    font-size: 16;
                    flex-weight: 1;
                }

                .mob-count {
                    font-size: 18;
                }

                .footer {
                    color: #A0E8FF;
                    font-size: 12;
                }

                .default-margin {
                    anchor-left: 10;
                    anchor-right: 10;
                }

            </style>

            <div class="active-session-hud">
                <div class="active-session-hud-container">

                    <div class="row-display" style="anchor-height: 20; anchor: 10;">
                        <p class="row-display-label center-label" style="color: #ca9f37;font-size: 15;">{{${'$'}mapName}}</p>
                    </div>

                    <div style="layout-mode: Full; anchor-height: 70">
                        <div class="row-display-wave-progress default-margin">
                            <p id="enemies-remaining" class="row-display-label center-label">Wave: {{${'$'}waveCount}}</p>
                        </div>

                        <div class="default-margin" style="anchor-top: 50;">
                            <p class="row-display-label center-label" style="font-size: 12; color: #ca9f37; font-weight: bold;">{{${'$'}waveState}}</p>
                        </div>
                    </div>

                    <div class="default-margin" style="flex-weight: 7; anchor-top: 10;">
                        {{#if waveState == WAITING_INTERVAL}}
                            <div style="center-label">
                                <div style="layout-mode: Full;">
                                    <progress id="interval-left-progress" class="circular-progress" value="{{${'$'}intervalLeftProgress}}"
                                              data-hyui-effect-height="0"
                                              data-hyui-effect-offset="0"
                                              data-hyui-color="#ffffff"
                                              style="anchor-width: 98; anchor-height: 98;">
                                    </progress>
                                    <div style="text-align: center;">
                                        <p style="font-size: 14; color: #7FD9F8; font-weight: bold;" id="interval-left" class="center-label">{{${'$'}intervalLeft}}</p>
                                    </div>
                                </div>
                            
                                <p>Preparing time!!</p>
                                <sprite src="Common/Spinner.png"
                                    data-hyui-frame-width="32"
                                    data-hyui-frame-height="32"
                                    data-hyui-frame-per-row="8"
                                    data-hyui-frame-count="72"
                                    data-hyui-fps="30"
                                    style="anchor-width: 32; anchor-height: 32; anchor-right: 1;">
                                </sprite>
                            </div>
                        {{/if}}
                        
                        {{#if waveState == WAITING_CLEAR}}
                            <div id="enemy-list" class="enemy-list">
                                <div style="layout-mode: Full;">
                                    <progress id="wave-progress" value="{{${'$'}waveProgressBar}}"
                                              data-hyui-effect-height="0"
                                              data-hyui-effect-offset="0">
                                    </progress>
                                    <div style="text-align: center;">
                                        <p style="font-size: 14; color: #7FD9F8; font-weight: bold;" id="wave-progress-label" class="center-label">Cleared: {{${'$'}waveProgressLabel}}</p>
                                    </div>
                                </div>
                                <div style="anchor-top: 20;"></div>
                                {{#each enemiesData}}
                                {{@enemyMob}}
                                {{/each}}
                            </div>
    
                        {{/if}}
                    </div>

                    <div class="default-margin" style="flex-direction: column; anchor-bottom: 15;">
                        <!--
                    <div id="RegressTimeBar">
                        <progress id="regressive-time-bar"
                                  value="{{${'$'}waveProgressBar}}"
                                  style="anchor-width: 98; anchor-height: 98;"
                                  data-hyui-direction="End">
                        </progress>
                            <progress id="regressive-bar" value="{{${'$'}waveProgressBar}}"
                                data-hyui-direction="end">
                            </progress>
                            <p class="center-label">progress bar reduzindo tempo</p>
                        </div>
                        -->
                        <div class="default-margin" style="flex-direction: row; anchor-top: 10;">
                            <div style="flex-weight: 1; text-align: left; layout-mode: left">
                                <p style="color: #7FD9F8; font-weight: bold;">Elapsed:</p>
                                <p id="elapsedTime" style="anchor-left: 5">{{${'$'}elapsedTime}}</p>
                            </div>
                            <div style="flex-weight: 1; text-align: right; layout-mode: right">
                                <p style="color: #7FD9F8; font-weight: bold;">Score: </p>
                                <p>{{${'$'}playerScore}}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        """.trimIndent()
        val enemyMobComponent = """
            <div class="mob-row"> 
                {{#if alives == 0}}
                    <p class="mob-text">{{${'$'}enemyType}}</p>
                    <p class="mob-count" style="color: #55FF55; font-weight: bold;">{{${'$'}killed}}</p>
                    <p class="mob-count" style="color: #55FF55;">/{{${'$'}totalEnemiesWave}}</p>
                {{else}}
                    <p class="mob-text">{{${'$'}enemyType}}</p>
                    <p class="mob-count" style="color: #FFAD42; font-weight: bold;">{{${'$'}killed}}</p>
                    <p class="mob-count" style="color: #FFAD42;">/{{${'$'}totalEnemiesWave}}</p>
                    
                {{/if}}
            </div>
        """.trimIndent()
    }
}