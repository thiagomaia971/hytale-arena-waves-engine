package com.miilhozinho.arenawavesengine.hud

import au.ellie.hyui.html.TemplateProcessor
import com.sun.org.apache.xalan.internal.xsltc.compiler.Template

class ActiveSessionHud{

    companion object {
        fun defaultTemplate(): TemplateProcessor {
            return TemplateProcessor()
                .registerComponent("enemyCard","""
                    <div class="mob-row"> 
                        <p class="mob-text">{{${'$'}enemyName}}</p>
                        <p class="mob-count">{{${'$'}countKilled}}/{{${'$'}countDef}}</p>
                    </div>
                """)
        }

        val html = """
<style>
    /* Estilos Gerais */
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
        text-align: center;
    }
    
    .center-label {
        anchor-top: 15;
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
        anchor-left: 50;
        anchor-right: 15;
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
        color: #FFFFFF;
        font-weight: bold;
        font-size: 16;
        anchor-right: 20;
        flex-weight: 0;
        text-align: right;
        vertical-align: right;
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
        <div class="row-display" style="anchor-height: 20; anchor: 10">
            <p class="row-display-label" style="color: #ca9f37;font-size: 15;">{{${'$'}mapName}}</p>
        </div>
        
        <div class="row-display-wave-progress default-margin" style="anchor-bottom: 5;">
            <p id="enemies-remaining" class="row-display-label center-label">Wave: {{${'$'}waveCount}}</p>
        </div>
            
        <div id="enemy-list" class="enemy-list default-margin" style="flex-weight: 7;">
            <div style="layout-mode: Full;">
                <progress id="wave-progress" value="{{${'$'}waveProgressBar}}" 
                    data-hyui-effect-height="20" 
                    data-hyui-effect-offset="0"
                    ">
                </progress>
                <div style="text-align: center;">
                    <p style="font-size: 14; color: #7FD9F8; font-weight: bold;" id="wave-progress-label">{{${'$'}waveProgressLabel}}</p>
                </div>
            </div>
            <div style="anchor-top: 20;"></div>
        </div>
        
        
        <div class="default-margin" style="flex-direction: column; anchor-bottom: 15;">
            <div style="text-align: center">
                progress bar reduzindo tempo
            </div>
            <div class="default-margin" style="flex-direction: row; anchor-top: 10;">
                <div style="flex-weight: 1; text-align: left">
                    <p style="color: #7FD9F8; font-weight: bold;">Elapsed: </p>
                    <p>01:15</p>
                </div>
                <div style="flex-weight: 1; text-align: right">
                    <p style="color: #7FD9F8; font-weight: bold;">Score: </p>
                    <p>{{${'$'}playerScore}}</p>
                </div>
            </div>
        </div>
    </div>
</div>
"""
    }
}