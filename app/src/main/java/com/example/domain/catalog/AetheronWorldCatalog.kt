package com.example.domain.catalog

import com.example.domain.model.*

object AetheronWorldCatalog {

    val factions = listOf(
        FactionInfo(
            id = "sylvaria_order",
            nameDe = "Orden der Bewahrer",
            homeContinentId = 1,
            ideologyDe = "Schützt die uralten Biomen-Wurzeln und die genetische Harmonie der Kreaturen auf Sylvaria.",
            perkDescriptionDe = "+20% Fangchance & +15% Vitalitäts-Trainingseffektivität ab Ruf 250."
        ),
        FactionInfo(
            id = "pyroclast_syndicate",
            nameDe = "Synthese-Syndikat",
            homeContinentId = 2,
            ideologyDe = "Nutzt Vulkanschmieden, Artefakt-Fusion und Hochleistungs-Training für maximale Kampfkraft auf Pyroclast.",
            perkDescriptionDe = "+20% Angriffs-Training & reduzierte Artefakt-Crafting-Kosten ab Ruf 250."
        ),
        FactionInfo(
            id = "aetheris_chroniclers",
            nameDe = "Freie Chronisten",
            homeContinentId = 3,
            ideologyDe = "Erforscht die schwebenden Inseln von Aetheris, Wetter-Anomalien und Legendäre Zuchtlinien.",
            perkDescriptionDe = "+25% Chance auf S/S+ Genetik-Rang beim Züchten & seltene Quest-Belohnungen ab Ruf 250."
        )
    )

    val biomes = listOf(
        // Continent 1: Sylvaria (ID 1)
        BiomeZone("syl_emerald_canopy", "Smaragd-Kronenwald", 1, "Leuchtender Urwald voller Terra- und Aero-Kreaturen.", ElementType.TERRA, listOf("mat_sylva_leaf", "mat_aether_shard"), 5, 0xFF1B4332),
        BiomeZone("syl_mist_lagoon", "Nebel-Lagune von Lyra", 1, "Tiefblaue Quellen, in denen Aqua- und Cryo-Monster laichen.", ElementType.AQUA, listOf("mat_coral_pearl", "mat_aether_shard"), 10, 0xFF004E64),
        BiomeZone("syl_spore_caverns", "Biolumineszente Sporenhöhlen", 1, "Unterirdisches Geflecht mit Toxin- und Umbra-Spezies.", ElementType.TOXIN, listOf("mat_glow_spore", "mat_iron_ore"), 15, 0xFF3C096C),
        BiomeZone("syl_sun_meadows", "Helios-Hochland", 1, "Goldene Ebenen mit Lux- und Volt-Läufern.", ElementType.LUX, listOf("mat_sun_amber", "mat_sylva_leaf"), 20, 0xFF5C4D1E),
        BiomeZone("syl_ancient_sanctum", "Wurzel-Heiligtum (Legenden-Zone)", 1, "Heiliger Hain der Legendären Wächter von Sylvaria.", ElementType.DRACO, listOf("mat_sun_amber", "art_solar_core"), 28, 0xFF2D6A4F),

        // Continent 2: Pyroclast (ID 2)
        BiomeZone("pyr_ash_caldera", "Asche-Kaldera", 2, "Glühende Lavafelder, Heimat aggressiver Pyro-Bestien.", ElementType.PYRO, listOf("mat_magma_core", "mat_iron_ore"), 30, 0xFF6A040F),
        BiomeZone("pyr_iron_peaks", "Ferro-Schmiedegipfel", 2, "Magnetische Erzgipfel voller gepanzerter Ferro- und Volt-Monster.", ElementType.FERRO, listOf("mat_iron_ore", "mat_volt_crystal"), 36, 0xFF37474F),
        BiomeZone("pyr_obsidian_dunes", "Obsidian-Wüste", 2, "Schwarze Glasdünen mit Sandstürmen und Draco-Jägern.", ElementType.DRACO, listOf("mat_magma_core", "mat_obsidian_scale"), 42, 0xFF4A2511),
        BiomeZone("pyr_sulfur_rift", "Schwefel-Abyss", 2, "Giftige Geysire und Schatten-Spalten voller Umbra-Kreaturen.", ElementType.UMBRA, listOf("mat_glow_spore", "mat_obsidian_scale"), 48, 0xFF311B47),
        BiomeZone("pyr_titan_throne", "Thron des Vulkantitanen (Legenden-Zone)", 2, "Urzeitlicher Vulkankern der Pyroclast-Legenden.", ElementType.PYRO, listOf("mat_obsidian_scale", "art_abyss_relic"), 55, 0xFF800F2F),

        // Continent 3: Aetheris (ID 3)
        BiomeZone("aet_sky_archipelago", "Schwebende Wolkeninseln", 3, "Windumtoste Inseln hoch über dem Meer.", ElementType.AERO, listOf("mat_volt_crystal", "mat_aether_prism"), 58, 0xFF023E8A),
        BiomeZone("aet_cryo_spire", "Kristall-Frostspitze", 3, "Ewiges Sterneneis mit Cryo- und Lux-Wächtern.", ElementType.CRYO, listOf("mat_coral_pearl", "mat_aether_prism"), 65, 0xFF0077B6),
        BiomeZone("aet_storm_citadel", "Zitadelle des Sturms", 3, "Blitzgeladene Ruinen einer untergegangenen Zivilisation.", ElementType.VOLT, listOf("mat_volt_crystal", "mat_iron_ore"), 72, 0xFF3A0CA3),
        BiomeZone("aet_ astral_void", "Astral-Graben", 3, "Raumzeit-Anomalie voller Aether- und Draco-Entitäten.", ElementType.AETHER, listOf("mat_aether_prism", "mat_obsidian_scale"), 80, 0xFF240046),
        BiomeZone("aet_chrono_nexus", "Chrono-Nexus (Legenden-Zone)", 3, "Das kosmische Zentrum aller drei Reiche.", ElementType.AETHER, listOf("mat_aether_prism", "art_chrono_prism"), 90, 0xFF10002B)
    )

    val geneticTraits = listOf(
        GeneticTrait("trait_solar_vein", "Solar-Ader", "+15% Angriff & +10% Krit-Chance bei Sonnenflut.", atkMult = 1.15f, critBonus = 0.10f),
        GeneticTrait("trait_obsidian_shell", "Obsidian-Panzer", "+20% Verteidigung gegen alle physischen & elementaren Treffer.", defMult = 1.20f),
        GeneticTrait("trait_chrono_reflex", "Chrono-Reflex", "+20% Geschwindigkeit & Erstschlag-Präzision.", spdMult = 1.20f, critBonus = 0.08f),
        GeneticTrait("trait_aether_overload", "Aether-Überladung", "+18% Angriff & +12% Geschwindigkeit.", atkMult = 1.18f, spdMult = 1.12f),
        GeneticTrait("trait_vampiric_fang", "Vampir-Klaue", "Heilt 15% des verursachten Schadens als KP.", atkMult = 1.10f, lifestealRatio = 0.15f),
        GeneticTrait("trait_titan_blood", "Titanen-Blut", "Vererbbares Legenden-Gen: +16% ATK, +16% DEF, +12% SPD.", atkMult = 1.16f, defMult = 1.16f, spdMult = 1.12f)
    )

    val items = listOf(
        GameItem("orb_aether_basic", "Aether-Sphäre", ItemCategory.CAPTURE_ORB, "Standard-Fangkapsel für wilde Monster (Fangbonus x1.2).", 2, 25),
        GameItem("orb_aether_master", "Hyper-Aether-Sphäre", ItemCategory.CAPTURE_ORB, "Hochpräzise Fangkapsel für seltene & legendäre Monster (Fangbonus x2.4).", 4, 55),
        GameItem("pot_vital_elixir", "Vital-Elixier", ItemCategory.POTION, "Stellt im Kampf oder auf Reisen sofort 120 KP & 50 Ausdauer wieder her.", 2, 120),
        GameItem("pot_phoenix_nectar", "Phönix-Nektar", ItemCategory.POTION, "Heilt ein Monster vollständig (350 KP & 100 Ausdauer).", 4, 350),
        // Crafting Materials
        GameItem("mat_sylva_leaf", "Sylva-Leuchtblatt", ItemCategory.CRAFTING_MAT, "Biolumineszentes Heilkraut aus den Wäldern von Sylvaria.", 1),
        GameItem("mat_aether_shard", "Aether-Splitter", ItemCategory.CRAFTING_MAT, "Reiner Energiesplitter zum Schmieden von Fang-Sphären.", 2),
        GameItem("mat_coral_pearl", "Tiefsee-Perle", ItemCategory.CRAFTING_MAT, "Kühlende Perle aus den Lagunen.", 2),
        GameItem("mat_glow_spore", "Nachtglanz-Spore", ItemCategory.CRAFTING_MAT, "Alchemistische Essenz aus dunklen Höhlen.", 2),
        GameItem("mat_sun_amber", "Helios-Bernstein", ItemCategory.CRAFTING_MAT, "Gespeicherte Sonnenenergie aus dem Hochland.", 3),
        GameItem("mat_magma_core", "Magma-Herzstein", ItemCategory.CRAFTING_MAT, "Glühender Kern aus der Asche-Kaldera von Pyroclast.", 3),
        GameItem("mat_iron_ore", "Ferro-Titanerz", ItemCategory.CRAFTING_MAT, "Unverwüstliches Erz für Rüstungen und Kapseln.", 2),
        GameItem("mat_volt_crystal", "Sturm-Quarz", ItemCategory.CRAFTING_MAT, "Knisternder Hochspannungskristall.", 3),
        GameItem("mat_obsidian_scale", "Drachen-Obsidian", ItemCategory.CRAFTING_MAT, "Seltene Schuppe aus Vulkanschluchten.", 4),
        GameItem("mat_aether_prism", "Astral-Prisma", ItemCategory.CRAFTING_MAT, "Kosmischer Kristall von den schwebenden Inseln auf Aetheris.", 4),
        // Evolution Artifacts
        GameItem("art_solar_core", "Artefakt: Solar-Kern", ItemCategory.EVOLUTION_ARTIFACT, "Erweckt die strahlende Licht-/Pyro-Evolutionsstufe eines Monsters.", 5, 1),
        GameItem("art_abyss_relic", "Artefakt: Abyss-Relikt", ItemCategory.EVOLUTION_ARTIFACT, "Entfesselt die verbotene Umbra-/Draco-Evolutionsform.", 5, 2),
        GameItem("art_chrono_prism", "Artefakt: Chrono-Prisma", ItemCategory.EVOLUTION_ARTIFACT, "Katalysiert die ultimative Aether-Evolution.", 5, 3),
        // Training Gear
        GameItem("gear_power_bracer", "Kraft-Armschiene", ItemCategory.GEAR_BOOST, "Steigert das Dojo-Training (+50% Bonus-Stat-Punkte pro Übung).", 4, 50)
    )

    val recipes = listOf(
        CraftingRecipe("rec_orb_basic", "orb_aether_basic", 2, mapOf("mat_aether_shard" to 1, "mat_iron_ore" to 1), 60),
        CraftingRecipe("rec_orb_master", "orb_aether_master", 1, mapOf("mat_aether_shard" to 2, "mat_volt_crystal" to 1, "mat_iron_ore" to 1), 220),
        CraftingRecipe("rec_vital_elixir", "pot_vital_elixir", 2, mapOf("mat_sylva_leaf" to 2, "mat_coral_pearl" to 1), 50),
        CraftingRecipe("rec_phoenix_nectar", "pot_phoenix_nectar", 1, mapOf("mat_sylva_leaf" to 2, "mat_sun_amber" to 1, "mat_glow_spore" to 1), 180),
        CraftingRecipe("rec_art_solar", "art_solar_core", 1, mapOf("mat_sun_amber" to 2, "mat_magma_core" to 1), 450),
        CraftingRecipe("rec_art_abyss", "art_abyss_relic", 1, mapOf("mat_obsidian_scale" to 2, "mat_glow_spore" to 2), 500),
        CraftingRecipe("rec_art_chrono", "art_chrono_prism", 1, mapOf("mat_aether_prism" to 2, "mat_volt_crystal" to 2), 650),
        CraftingRecipe("rec_gear_bracer", "gear_power_bracer", 1, mapOf("mat_iron_ore" to 2, "mat_magma_core" to 1), 350)
    )

    val defaultPlayerSkills = listOf(
        PlayerSkillNode("skill_tamer_mastery", "Entdecker & Fänger", "Meister der Sphären", "+8% Fangrate pro Rang & höhere Chance auf seltene Wetter-Spawns.", 5, 1),
        PlayerSkillNode("skill_dojo_mentor", "Dojo & Evolution", "Dojo-Großmeister", "+15% zusätzliche Attribut-Punkte (ATK/DEF/SPD) aus Trainings-Minispielen pro Rang.", 5, 1),
        PlayerSkillNode("skill_genetic_savant", "Zucht & Genetik", "Genetik-Architekt", "Erhöht die Wahrscheinlichkeit auf S/S+ Nachkommen und Legenden-Gen-Vererbung.", 5, 1),
        PlayerSkillNode("skill_tactical_commander", "Kampftaktik", "Feldherr der Elemente", "+6% Elementarschaden & +10 Start-Ausdauer in allen Rundenkämpfen pro Rang.", 5, 1),
        PlayerSkillNode("skill_alchemist_guild", "Crafting & Alchemie", "Runen-Schmied", "+20% Ressourcen-Ausbeute in Biomen & Bonus-Ertrag beim Crafting pro Rang.", 5, 0)
    )

    // 24 Gym Badges (8 per Continent)
    val gymBadges: List<GymLeaderBadge> = buildList {
        val leadersC1 = listOf(
            Triple("Wurzel-Orden", "Hüterin Lyra", ElementType.TERRA),
            Triple("Gezeiten-Orden", "Kapitän Nereus", ElementType.AQUA),
            Triple("Zephir-Orden", "Meisterin Vael", ElementType.AERO),
            Triple("Sporen-Orden", "Alchemist Mykor", ElementType.TOXIN),
            Triple("Bernstein-Orden", "Archon Solis", ElementType.LUX),
            Triple("Gewitter-Orden", "Kommandant Rai", ElementType.VOLT),
            Triple("Frosthain-Orden", "Seherin Freya", ElementType.CRYO),
            Triple("Sylvaria-Krone", "Großbewahrer Thorne", ElementType.DRACO)
        )
        leadersC1.forEachIndexed { idx, (badge, leader, elem) ->
            add(
                GymLeaderBadge(
                    badgeIndex = idx + 1,
                    badgeNameDe = badge,
                    leaderNameDe = leader,
                    continentId = 1,
                    element = elem,
                    recommendedLevel = 8 + idx * 3,
                    aceSpeciesId = (idx * 9 + 3).coerceIn(1, 80),
                    mottoDe = "Nur wer im Einklang mit den Genen und Elementen kämpft, besteht auf Sylvaria!"
                )
            )
        }

        val leadersC2 = listOf(
            Triple("Magma-Orden", "Schmiedelord Ignis", ElementType.PYRO),
            Triple("Stahlwall-Orden", "Kastellanin Varka", ElementType.FERRO),
            Triple("Obsidian-Orden", "Kriegsherr Drakon", ElementType.DRACO),
            Triple("Schatten-Orden", "Inquisitorin Nyx", ElementType.UMBRA),
            Triple("Volt-Reaktor-Orden", "Ingenieur Tesla", ElementType.VOLT),
            Triple("Giftnebel-Orden", "Vipera Kael", ElementType.TOXIN),
            Triple("Sonnensturm-Orden", "Imperator Helios", ElementType.LUX),
            Triple("Pyroclast-Siegel", "Hochsyndikus Vulcan", ElementType.PYRO)
        )
        leadersC2.forEachIndexed { idx, (badge, leader, elem) ->
            add(
                GymLeaderBadge(
                    badgeIndex = 9 + idx,
                    badgeNameDe = badge,
                    leaderNameDe = leader,
                    continentId = 2,
                    element = elem,
                    recommendedLevel = 32 + idx * 3,
                    aceSpeciesId = (81 + idx * 9 + 2).coerceIn(81, 160),
                    mottoDe = "Auf Pyroclast zählt nur geschmiedete Disziplin und kompromissloses Stat-Training!"
                )
            )
        }

        val leadersC3 = listOf(
            Triple("Stratosphären-Orden", "Himmelsadmiral Zephyr", ElementType.AERO),
            Triple("Sternenfrost-Orden", "Orakel Glacia", ElementType.CRYO),
            Triple("Blitzkrone-Orden", "Valiant Storm", ElementType.VOLT),
            Triple("Chrono-Stahl-Orden", "Wächter Aegis", ElementType.FERRO),
            Triple("Eklipsen-Orden", "Lady Noctis", ElementType.UMBRA),
            Triple("Astral-Licht-Orden", "Seraphina Lux", ElementType.LUX),
            Triple("Welten-Drachen-Orden", "Ältester Bahamut", ElementType.DRACO),
            Triple("Ewiger Aether-Orden", " Großchronist Orion", ElementType.AETHER)
        )
        leadersC3.forEachIndexed { idx, (badge, leader, elem) ->
            add(
                GymLeaderBadge(
                    badgeIndex = 17 + idx,
                    badgeNameDe = badge,
                    leaderNameDe = leader,
                    continentId = 3,
                    element = elem,
                    recommendedLevel = 58 + idx * 4,
                    aceSpeciesId = (161 + idx * 9 + 2).coerceIn(161, 240),
                    mottoDe = "Jenseits der Wolken von Aetheris entscheidet die Perfektion deiner Zucht- und Evolutionslinie!"
                )
            )
        }
    }

    val worldNpcs = listOf(
        WorldNpc(
            id = "npc_elara",
            nameDe = "Chronistin Elara",
            roleDe = "Fraktions-Gesandte & Questgeberin",
            continentId = 1,
            factionId = "sylvaria_order",
            dawnLocation = 2 to 2,
            dayLocation = 4 to 3,
            duskLocation = 6 to 4,
            nightLocation = 3 to 6,
            dialogueByTime = mapOf(
                TimeOfDay.DAWN to "Im Morgengrauen erwachen die seltenen Aero-Kreaturen im Kronenwald. Prüfe das Quest-Board!",
                TimeOfDay.DAY to "Der Orden der Bewahrer sucht Trainer, die das Gleichgewicht der Biome sichern.",
                TimeOfDay.DUSK to "In der Dämmerung ziehen Drachen-Spezies zu den Aether-Quellen.",
                TimeOfDay.NIGHT to "Vorsicht bei Nacht: Umbra-Monster sind jetzt deutlich stärker und häufiger!"
            )
        ),
        WorldNpc(
            id = "npc_kaelen",
            nameDe = "Dojo-Meister Kaelen",
            roleDe = "Experte für Attribut-Training & Evolution",
            continentId = 2,
            factionId = "pyroclast_syndicate",
            dawnLocation = 5 to 2,
            dayLocation = 5 to 5,
            duskLocation = 4 to 6,
            nightLocation = 2 to 5,
            dialogueByTime = mapOf(
                TimeOfDay.DAWN to "Frühes Kraft-Training schmiedet den Angriffswert deiner Monster!",
                TimeOfDay.DAY to "Wusstest du? Wenn du im Dojo Angriff oder Verteidigung gezielt über +30 trainierst, schaltest du Spezial-Evolutionen frei!",
                TimeOfDay.DUSK to "Nach einem S-Rang im Mini-Spiel entwickeln sich Monster oft zu wahren Champions.",
                TimeOfDay.NIGHT to "Selbst im Schlaf regenerieren gut trainierte Monster schneller ihre Ausdauer."
            )
        ),
        WorldNpc(
            id = "npc_lyssandra",
            nameDe = "Genetikerin Lyssandra",
            roleDe = "Leiterin des Zucht- & Legenden-Labors",
            continentId = 3,
            factionId = "aetheris_chroniclers",
            dawnLocation = 6 to 2,
            dayLocation = 3 to 3,
            duskLocation = 5 to 4,
            nightLocation = 6 to 6,
            dialogueByTime = mapOf(
                TimeOfDay.DAWN to "Die kosmische Strahlung am Morgen begünstigt S+ Gene bei neuen Eiern!",
                TimeOfDay.DAY to "Selbst ein gefangenes Legendäres Monster kann in unserem Labor durch Zucht in eine noch mächtigere Generation (Gen 2+) aufsteigen!",
                TimeOfDay.DUSK to "Kombiniere Solar-Ader und Chrono-Reflex für unaufhaltsame Nachkommen.",
                TimeOfDay.NIGHT to "Bei Aether-Nebel und Nacht erscheinen die Hüter des Chrono-Nexus."
            )
        )
    )

    // Deterministic 240 Monster Species Catalog (80 per Continent)
    val allSpecies: List<MonsterSpecies> by lazy {
        val list = mutableListOf<MonsterSpecies>()

        val sylvariaRoots = listOf(
            "Sylph" to ElementType.TERRA, "Aquil" to ElementType.AQUA, "Zephy" to ElementType.AERO,
            "Myco" to ElementType.TOXIN, "Helio" to ElementType.LUX, "Volta" to ElementType.VOLT,
            "Cryon" to ElementType.CRYO, "Briar" to ElementType.TERRA, "Naiad" to ElementType.AQUA,
            "Gale" to ElementType.AERO, "Veno" to ElementType.TOXIN, "Lumi" to ElementType.LUX,
            "Spark" to ElementType.VOLT, "Frost" to ElementType.CRYO, "Thorn" to ElementType.TERRA,
            "Tide" to ElementType.AQUA, "Glade" to ElementType.TERRA, "Dusk" to ElementType.UMBRA,
            "Ironbough" to ElementType.FERRO, "Verdant" to ElementType.DRACO, "Moss" to ElementType.TERRA,
            "brook" to ElementType.AQUA, "Breeze" to ElementType.AERO, "Spore" to ElementType.TOXIN,
            "Dawn" to ElementType.LUX, "Arc" to ElementType.VOLT
        )

        val pyroclastRoots = listOf(
            "Pyro" to ElementType.PYRO, "Ferro" to ElementType.FERRO, "Draco" to ElementType.DRACO,
            "Umbra" to ElementType.UMBRA, "Magma" to ElementType.PYRO, "Titan" to ElementType.FERRO,
            "Cinder" to ElementType.PYRO, "Obsid" to ElementType.DRACO, "Nyx" to ElementType.UMBRA,
            "Sulfur" to ElementType.TOXIN, "Anvil" to ElementType.FERRO, "Blaze" to ElementType.PYRO,
            "Wyrm" to ElementType.DRACO, "Shade" to ElementType.UMBRA, "Voltforge" to ElementType.VOLT,
            "Scorch" to ElementType.PYRO, "Basalt" to ElementType.TERRA, "Slag" to ElementType.FERRO,
            "Ash" to ElementType.AERO, "Ember" to ElementType.PYRO, "Smelt" to ElementType.FERRO,
            "Drake" to ElementType.DRACO, "Void" to ElementType.UMBRA, "Fume" to ElementType.TOXIN,
            "Flare" to ElementType.LUX, "Krak" to ElementType.PYRO
        )

        val aetherisRoots = listOf(
            "Aether" to ElementType.AETHER, "Astral" to ElementType.LUX, "Chrono" to ElementType.AETHER,
            "Strato" to ElementType.AERO, "Glaci" to ElementType.CRYO, "Tempest" to ElementType.VOLT,
            "Nebula" to ElementType.UMBRA, "Celest" to ElementType.LUX, "Star" to ElementType.AETHER,
            "Cosmos" to ElementType.DRACO, "Zero" to ElementType.CRYO, "Pulse" to ElementType.VOLT,
            "Vortex" to ElementType.AERO, "Aegis" to ElementType.FERRO, "Eclipse" to ElementType.UMBRA,
            "Nova" to ElementType.PYRO, "Quasar" to ElementType.AETHER, "Comet" to ElementType.CRYO,
            "Zenith" to ElementType.LUX, "Rift" to ElementType.UMBRA, "Halo" to ElementType.LUX,
            "ether" to ElementType.AETHER, "Sky" to ElementType.AERO, "Rune" to ElementType.FERRO,
            "Myth" to ElementType.DRACO, "Omega" to ElementType.AETHER
        )

        fun buildContinentSpecies(continentId: Int, startId: Int, roots: List<Pair<String, ElementType>>) {
            val continentBiomes = biomes.filter { it.continentId == continentId }
            var currentId = startId
            // 25 3-stage families = 75 monsters (with branching evolutions on stage 1 -> stage 2/3)
            for (familyIdx in 0 until 25) {
                val (rootRaw, primaryElem) = roots[familyIdx % roots.size]
                val root = rootRaw.replaceFirstChar { it.uppercase() }
                val secondaryElem = ElementType.entries[(familyIdx + continentId * 3) % ElementType.entries.size]
                    .takeIf { it != primaryElem }
                val biome = continentBiomes[familyIdx % (continentBiomes.size - 1)]
                val trait = geneticTraits[(familyIdx + continentId) % (geneticTraits.size - 1)]
                val weatherPref = WeatherType.entries[(familyIdx + continentId) % WeatherType.entries.size]
                val timePref = TimeOfDay.entries[familyIdx % TimeOfDay.entries.size]

                val id1 = currentId
                val id2 = currentId + 1
                val id3 = currentId + 2
                val name1 = "${root}ling"
                val name2 = "${root}claw"
                val name3 = "${root}lord"

                val artifactForFamily = when (continentId) {
                    1 -> "art_solar_core"
                    2 -> "art_abyss_relic"
                    else -> "art_chrono_prism"
                }

                // Stage 1: Evolves into Stage 2 via Level + Attack/Speed training OR directly into Stage 3 via Evolution Artifact
                val stage1 = MonsterSpecies(
                    id = id1,
                    nameDe = name1,
                    continentId = continentId,
                    biomeId = biome.id,
                    primaryElement = primaryElem,
                    secondaryElement = null,
                    baseHp = 48 + (continentId * 6) + (familyIdx % 12),
                    baseAtk = 42 + (continentId * 5) + (familyIdx % 10),
                    baseDef = 40 + (continentId * 5) + ((familyIdx * 3) % 10),
                    baseSpd = 44 + (continentId * 4) + ((familyIdx * 2) % 11),
                    baseElem = 45 + (continentId * 5) + (familyIdx % 9),
                    stage = 1,
                    isLegendary = false,
                    preferredWeather = weatherPref,
                    preferredTime = timePref,
                    signatureTrait = trait,
                    skills = createSkillsFor(primaryElem, null, 1),
                    evolutions = listOf(
                        EvolutionBranch(
                            targetSpeciesId = id2,
                            targetSpeciesName = name2,
                            minLevel = 14 + (continentId - 1) * 8,
                            requiredDominantStat = if (familyIdx % 2 == 0) StatType.ATK else StatType.SPD,
                            minTrainedStatValue = 15,
                            conditionDescriptionDe = "Level ${14 + (continentId - 1) * 8} + Dojo-Training (${if (familyIdx % 2 == 0) "ATK" else "SPD"} ≥ 15)"
                        ),
                        EvolutionBranch(
                            targetSpeciesId = id3,
                            targetSpeciesName = name3,
                            minLevel = 20 + (continentId - 1) * 8,
                            requiredArtifactId = artifactForFamily,
                            conditionDescriptionDe = "Sofortige Artefakt-Erweckung mit ${items.find { it.id == artifactForFamily }?.nameDe ?: "Artefakt"}"
                        )
                    ),
                    loreDe = "Eine junge Kreatur aus dem Biom ${biome.nameDe}. Ihr Entwicklungspfad reagiert stark auf gezieltes Dojo-Training."
                )

                // Stage 2: Evolves into Stage 3 via Level + trained stats or Artifact
                val stage2 = MonsterSpecies(
                    id = id2,
                    nameDe = name2,
                    continentId = continentId,
                    biomeId = biome.id,
                    primaryElement = primaryElem,
                    secondaryElement = secondaryElem,
                    baseHp = stage1.baseHp + 24,
                    baseAtk = stage1.baseAtk + 22,
                    baseDef = stage1.baseDef + 20,
                    baseSpd = stage1.baseSpd + 19,
                    baseElem = stage1.baseElem + 23,
                    stage = 2,
                    isLegendary = false,
                    preferredWeather = weatherPref,
                    preferredTime = timePref,
                    signatureTrait = trait,
                    skills = createSkillsFor(primaryElem, secondaryElem, 2),
                    evolutions = listOf(
                        EvolutionBranch(
                            targetSpeciesId = id3,
                            targetSpeciesName = name3,
                            minLevel = 28 + (continentId - 1) * 10,
                            requiredDominantStat = StatType.DEF,
                            minTrainedStatValue = 30,
                            conditionDescriptionDe = "Level ${28 + (continentId - 1) * 10} + Dojo-Meisterschaft (Trainierte Punkte ≥ 30)"
                        ),
                        EvolutionBranch(
                            targetSpeciesId = id3,
                            targetSpeciesName = name3,
                            minLevel = 22 + (continentId - 1) * 10,
                            requiredArtifactId = artifactForFamily,
                            conditionDescriptionDe = "Artefakt-Resonanz (${items.find { it.id == artifactForFamily }?.nameDe ?: "Artefakt"})"
                        )
                    ),
                    loreDe = "Durch Kämpfe und diszipliniertes Training gereifte Form von $name1. Beherrscht kombinierte Elementarangriffe."
                )

                // Stage 3: Final form
                val stage3 = MonsterSpecies(
                    id = id3,
                    nameDe = name3,
                    continentId = continentId,
                    biomeId = biome.id,
                    primaryElement = primaryElem,
                    secondaryElement = secondaryElem ?: ElementType.AETHER,
                    baseHp = stage2.baseHp + 32,
                    baseAtk = stage2.baseAtk + 30,
                    baseDef = stage2.baseDef + 28,
                    baseSpd = stage2.baseSpd + 26,
                    baseElem = stage2.baseElem + 32,
                    stage = 3,
                    isLegendary = false,
                    preferredWeather = weatherPref,
                    preferredTime = timePref,
                    signatureTrait = trait,
                    skills = createSkillsFor(primaryElem, secondaryElem ?: ElementType.AETHER, 3),
                    evolutions = emptyList(),
                    loreDe = "Die vollkommene Entwicklungsstufe der ${root}-Linie. Ein gefürchteter Champion in den Arenen des Kontinents."
                )

                list.add(stage1)
                list.add(stage2)
                list.add(stage3)
                currentId += 3
            }

            // Plus 5 Unique Continent Legendaries (75 + 5 = 80 monsters per continent!)
            val legendaryBiome = continentBiomes.last()
            val legendaryNames = when (continentId) {
                1 -> listOf("Sylvarion-Rex", "Nereidra-Prime", "Helios-Archon", "Verdant-Titan", "Aether-Dryade")
                2 -> listOf("Vulcanos-Prime", "Obsidian-Bahamut", "Nyx-Leviathan", "Ferro-Colossus", "Ignis-Sovereign")
                else -> listOf("Chrono-Seraph", "Astral-Ouroboros", "Tempest-Valiant", "Cryo-Eternus", "Aetheron-Omega")
            }
            val legendaryElements = when (continentId) {
                1 -> listOf(ElementType.TERRA, ElementType.AQUA, ElementType.LUX, ElementType.DRACO, ElementType.AETHER)
                2 -> listOf(ElementType.PYRO, ElementType.DRACO, ElementType.UMBRA, ElementType.FERRO, ElementType.PYRO)
                else -> listOf(ElementType.AETHER, ElementType.DRACO, ElementType.VOLT, ElementType.CRYO, ElementType.AETHER)
            }

            for (legIdx in 0 until 5) {
                val legId = currentId + legIdx
                val legElem = legendaryElements[legIdx]
                list.add(
                    MonsterSpecies(
                        id = legId,
                        nameDe = legendaryNames[legIdx],
                        continentId = continentId,
                        biomeId = legendaryBiome.id,
                        primaryElement = legElem,
                        secondaryElement = ElementType.AETHER,
                        baseHp = 128 + continentId * 12 + legIdx * 4,
                        baseAtk = 115 + continentId * 10 + legIdx * 4,
                        baseDef = 110 + continentId * 10 + legIdx * 3,
                        baseSpd = 108 + continentId * 9 + legIdx * 4,
                        baseElem = 125 + continentId * 11 + legIdx * 5,
                        stage = 3,
                        isLegendary = true,
                        preferredWeather = WeatherType.AETHER_MIST,
                        preferredTime = TimeOfDay.NIGHT,
                        signatureTrait = geneticTraits.last(), // Titanen-Blut
                        skills = createSkillsFor(legElem, ElementType.AETHER, 4),
                        evolutions = emptyList(),
                        loreDe = "LEGENDÄRES URWESEN (#$legId): Kann nach dem Fang im Zucht-Labor verpaart werden, um Nachkommen der Generation 2+ mit noch mächtigeren Titanen-Werten hervorzubringen!"
                    )
                )
            }
        }

        buildContinentSpecies(1, 1, sylvariaRoots)
        buildContinentSpecies(2, 81, pyroclastRoots)
        buildContinentSpecies(3, 161, aetherisRoots)
        list
    }

    fun getSpecies(id: Int): MonsterSpecies {
        return allSpecies.getOrElse((id - 1).coerceIn(0, allSpecies.lastIndex)) { allSpecies.first() }
    }

    fun getContinentName(continentId: Int): String = when (continentId) {
        1 -> "Sylvaria (Smaragd-Kontinent)"
        2 -> "Pyroclast (Vulkanschmiede-Reich)"
        else -> "Aetheris (Himmels-Archipel)"
    }

    private fun createSkillsFor(primary: ElementType, secondary: ElementType?, tier: Int): List<CombatSkill> {
        val basePower = 35 + tier * 18
        val list = mutableListOf(
            CombatSkill(
                id = "sk_${primary.name.lowercase()}_strike_$tier",
                nameDe = "${primary.labelDe}-Schlag",
                element = primary,
                power = basePower,
                staminaCost = 15,
                descriptionDe = "Schneller ${primary.labelDe}-Angriff mit verlässlicher Trefferrate."
            ),
            CombatSkill(
                id = "sk_${primary.name.lowercase()}_nova_$tier",
                nameDe = "${primary.labelDe}-Eruption",
                element = primary,
                power = basePower + 28,
                staminaCost = 32,
                descriptionDe = "Mächtige ${primary.labelDe}-Entladung. Profitiert stark von passendem Wetter."
            ),
            CombatSkill(
                id = "sk_focus_stance_$tier",
                nameDe = "Aether-Fokus",
                element = secondary ?: primary,
                power = basePower - 10,
                staminaCost = 18,
                descriptionDe = "Taktischer Treffer, der gleichzeitig den eigenen Angriff im Kampf stärkt.",
                buffsSelfStat = StatType.ATK
            )
        )
        if (secondary != null) {
            list.add(
                CombatSkill(
                    id = "sk_${secondary.name.lowercase()}_cataclysm_$tier",
                    nameDe = "${secondary.labelDe}-Kataklysmus",
                    element = secondary,
                    power = basePower + 42,
                    staminaCost = 45,
                    descriptionDe = "Ultimativer Dual-Element-Finisher mit enormem Durchbruchspotenzial."
                )
            )
        } else {
            list.add(
                CombatSkill(
                    id = "sk_${primary.name.lowercase()}_overdrive_$tier",
                    nameDe = "${primary.labelDe}-Overdrive",
                    element = primary,
                    power = basePower + 38,
                    staminaCost = 40,
                    descriptionDe = "Hochkonzentrierter Spezialangriff."
                )
            )
        }
        return list
    }
}
