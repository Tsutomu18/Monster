# Aetheron: Chronicles of the Three Realms

Ein taktischer Monster-Taming- und Open-World-RPG-Titel im Geist von *Coromon*, *Pokémon* und *PokeOne*, entwickelt für präzise Touch-Steuerung und nahtloses Cross-Device-Cloud-Spielen. Spieler erkunden drei riesige Kontinente mit 24 Arena-Orden, fangen und züchten 240 einzigartige Kreaturen, absolvieren interaktive Trainings-Minispiele zur gezielten Attribut- und Evolutionssteuerung, erfüllen dynamisch generierte Fraktions-Quests und messen sich im Online-Handel sowie in der globalen PvP-Arena.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Bestätigte & Empfohlene Produktentscheidungen vor dem Build:**
> Da du den Fokus direkt auf das **interaktive Monster-Trainingssystem** und das **dynamische Quest-System** erweitert hast, nutzen wir für die offenen Architekturfragen folgende empfohlene Standardwerte:

- **Grafik & Perspektive (Empfohlener Standard)**: **2D Top-Down Taktik-RPG-Perspektive (Coromon-inspiriert)** auf Basis einer hochperformanten Jetpack-Compose-Canvas-Tile-Engine mit dynamischen Licht-/Schatten-Layern für Tag/Nacht, Partikeleffekten für Wetter und animierten Kampfszenen.
- **Cloud-Speicher, Online-Handel & PvP-Arena (Empfohlener Standard)**: **Echtzeit-Cloud-Integration via Firebase Firestore & Google Sign-In**. Dein Spielstand, deine 240er-Kreaturensammlung, der globale Tausch-Marktplatz und die Online-Arena- Herausforderungskämpfe werden in Echtzeit über alle Geräte synchronisiert.
- **Welt- & Monster-Umfang (Empfohlener Standard)**: **Alle 3 Kontinente (*Sylvaria*, *Pyroclast*, *Aetheris*) sind von Beginn an bereisbar bzw. freischaltbar**, inklusive **24 Arena-Orden** (8 pro Kontinent), **240 einzigartigen Monsterspezies** (80 pro Kontinent) mit eigenen Elementen, Fähigkeiten, genetischen Merkmalen und mehrstufigen Evolutionsbäumen.
- **NEU — Interaktives Monster-Trainingssystem**: Gezieltes EV/Stat-Training (Angriff, Verteidigung, Elementarkraft, Fokus, Geschwindigkeit, Ausdauer) über **3 spielbare interaktive Mini-Games** (Reflex-Schlagtraining, Schild-Rhythmus-Abwehr, Aether-Sprint-Parcours), deren Leistung direkte Bonus-Punkte, seltene Trainings-Belohnungen und **spezielle Evolutionszweige** freischaltet.
- **NEU — Dynamischer Quest-Generator & Fraktionspolitik**: Prozedurale und kontextsensitive Quest-Engine, die Missionen (Monsterjagden, Ressourcen-Sammlung, Biom-Erkundung, Artefakt-Bergung) anhand von **aktuellem Standort/Biom, Spielerstufe, abgeschlossenen Quests, Fraktionsruf und lokal aktiven Monstern** generiert und Schwierigkeitsgrad-skalierte Belohnungen vergibt.

---

## 1. Overview & Core Concept

- **Was die App bietet**:
  1. **Lebendige 3-Kontinente-Welt & 24 Orden**: Erkundung verschiedener Biome (Urwälder, Vulkankrater, Kristallwüsten, Tiefsee-Küsten, Himmelsruinen) mit **dynamischem Wetter** (Sonne, Starkregen, Gewitter, Sandsturm, Aether-Nebel) und **Echtzeit-/Spielzeit-Tag-Nacht-Zyklus**, die sowohl Monster-Erscheinungsraten als auch Kampfeffekte direkt verändern. NPCs folgen individuellen **Tagesabläufen** (Morgendlicher Markt, Training am Mittag, Taverne/Forschung bei Nacht).
  2. **Taktisches Runden-Kampfsystem**: Strategische Duelle mit Ausdauer-/AP-Management, 12 Elementartypen, Wetter-Resonanzen, vererbbaren genetischen Eigenschaften (*Traits*) und grafisch animierten Attacken-Effekten.
  3. **Komplexes Evolutions-, Artefakt- & Züchtungssystem**: 240 Monster sammeln EP und entwickeln sich über verzweigte Pfade weiter – gesteuert durch **trainierte Statuswerte** und **seltene Evolutions-Artefakte** (z. B. *Solar-Kern*, *Abyss-Relikt*, *Chrono-Prisma*). In der Zuchtstation lassen sich zwei Kreaturen (inkl. gefangener **Legendärer Monster**) paaren, um Nachkommen mit vererbten Fähigkeiten, höheren Potenzial-Genen (S-Rang) und verstärkten Basiswerten zu züchten.
  4. **Interaktives Monster-Trainingssystem (Mini-Games)**: Aktives Dojo-Training mit 3 geschicklichkeitsbasierten Übungen, die individuelle Statistiken steigern, Leistungsränge (Bronze bis Meister) bewerten und die individuelle Evolutionsrichtung eines Monsters prägen.
  5. **Dynamisches Quest-, Fraktions- & Politiksystem**: Drei konkurrierende Großfraktionen (*Orden der Bewahrer*, *Synthese-Syndikat*, *Freie Chronisten*) mit politischem Einfluss pro Kontinent, eigenen Story-Questreihen und einem dynamischen Quest-Board, das sich an Level, Biom, Fraktionsrang und lokale Monsterfauna anpasst.
  6. **Crafting & Charakter-Skilltree**: Herstellung von Fang-Kapseln (*Aether-Sphären*), Tränken, Trainingsgewichten, Ausrüstung und Evolutions-Artefakten aus Biom-Ressourcen sowie ein **Spieler-Skill-System** (Entdecker-, Züchter-, Taktiker- und Alchemie-Pfade).
  7. **Cloud-Sync, Online-Tauschbörse & PvP-Arena**: Nahtlose Speicherung über Firebase Firestore, Echtzeit-Kreaturentausch zwischen Spielern und Online-Arena-Kämpfe gegen echte Spieler-Teams.

- **Zielgruppe**: Fans von tiefgründigen Monster-Sammel-RPGs (*Coromon*, *Pokémon*, *Monster Sanctuary*), die taktische Tiefe, Genetik-Optimierung und eine lebendige Welt auf Mobilgeräten und Tablets/Desktops suchen.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Anmeldung & Cloud-Synchronisation**: Startbildschirm mit atmosphärischem Artwork -> Google Sign-In -> Automatisches Laden des Cloud-Spielstands (oder Starter-Auswahl auf Kontinent 1: *Sylvaria*).
2. **Welt-Erkundung & HUD**: Touch-optimiertes virtuelles Steuerkreuz (oder Direkt-Tap-Steuerung) auf der 2D-Oberweltkarte; kompaktes Top-HUD zeigt aktuelles Biom, Kontinent, Uhrzeit (Tag/Nacht-Symbol), aktives Wetter, Fraktions-Einfluss und 1-Tap-Schnellzugriff auf Team, Quest-Log und Karte.
3. **Begegnung & Taktischer Kampf**: Wildes Monster oder Arena-Leiter -> Wechsel in die animierte Kampfbühne mit Wetter-Partikeln, HP/Ausdauer-Balken, Typen-Effektivitäts-Indikator, Attacken-Auswahl, Artefakt-/Item-Beutel und Fang-Mechanik.
4. **Dojo-Training & Evolution**: Auswahl eines Team-Monsters -> Wahl des Ziel-Attributs (z. B. Angriff oder Geschwindigkeit) -> Spielen des interaktiven 20-Sekunden-Trainings-Minispiels -> Auswertung (S-Rang-Multiplikator) -> Direkter Stat-Zuwachs + Vorschau, wie sich das Training auf die nächste Evolutionsform auswirkt.
5. **Zucht-Labor & Genetik**: Auswahl zweier kompatibler Eltern (auch Legendäre Monster zur Potenzial-Verstärkung) -> Vorschau der vererbbaren *Genetic Traits* und Stat-Maxima -> Ausbrüten des gestärkten Nachkommen.
6. **Dynamisches Quest-Board & Crafting-Schmiede**: Annahme kontextbasierter Quests im aktuellen Biom -> Sammeln seltener Materialien / Besiegen von Ziel-Monstern -> Einlösen skalierten Loots & Fraktionsrufs -> Craften von Ausrüstung und Evolutions-Artefakten.
7. **Online-Hub (Tausch & Arena)**: Einstellen oder Annehmen von Monster-Tauschangeboten im globalen Firestore-Handelsnetzwerk sowie Herausfordern von registrierten Spieler-Teams in der Online-Arena.

### Visual Identity & Theme
- **Aesthetic Direction**: **Tactical Aether-Punk RPG** – dunkle, kontrastreiche Edelstein- und Schiefer-Oberflächen (inspiriert von *Coromon* und modernen Taktik-RPGs), kombiniert mit leuchtenden Elementar-Akzenten und klaren Status-Anzeigen.
- **Color Palette & Mood**:
  - `--background`: `#0B101B` (Tiefes Mitternachts-Schieferblau für maximale Leuchtkraft der Spielwelt und Monster-Sprites)
  - `--surface`: `#151E30` / `#1E2942` (Getönte Taktik-Karten mit feinen metallischen Rahmen)
  - `--primary`: `#00E5FF` (Aether-Cyan für Hauptaktionen, Energie und UI-Fokus)
  - `--secondary`: `#FFB300` (Orden-Gold für Meisterschaften, Quests, Zucht-Gene und Legendäre Ränge)
  - `--accent`: `#FF4081` & Elementar-Farbcodes (Pyro-Rot `#FF5252`, Terra-Grün `#69F0AE`, Volt-Gelb `#FFD740`, Abyss-Violett `#B388FF`)
- **Typography & Hierarchy**:
  - **Display / Header Font**: *Cinzel* oder *Orbitron* (lokal gebündelt via `font-util` in `res/font/`) für epische Kontinent-Namen, Arena-Orden und Kampf-Banner.
  - **Body & Tactical Stats Font**: *Plus Jakarta Sans* & *JetBrains Mono* für gestochen scharfe Statuswerte, Schadenszahlen, Genetik-Prozente und Quest-Beschreibungen.
- **Component Styling & Adaptive Layout**:
  - Vollbild-Edge-to-Edge-Layout mit sicheren System-Insets.
  - Auf Smartphones (`Compact`) schnelles Bottom-Dock für *Welt*, *Team & Training*, *Zucht & Lexikon (240)*, *Quests & Crafting* und *Online-Hub*. Auf Tablets/großen Bildschirmen (`Expanded`) zweispaltiges Taktik-Dashboard mit permanenter Minimap und Team-Statusleiste.

---

## 3. Key Product Decisions & Trade-Offs

- **Entscheidung 1: 240 Monster & 3 Kontinente deterministisch + datengetrieben modellieren**
  - *Gewählter Ansatz*: Eine strukturierte, deterministische Monster- und Welt-Datenbank, die alle **240 Spezies** (80 pro Kontinent: *Sylvaria*, *Pyroclast*, *Aetheris*) mit individuellen Basiswerten, Elementartypen, einzigartigen Fähigkeiten, Evolutionsbedingungen (Level, trainierter Fokus-Stat oder Artefakt) und mehrteiligen Vektor-/Canvas-Kreaturendesigns bereitstellt.
  - *Warum*: Garantiert, dass alle 3 Kontinente, 24 Orden und 240 Monster sofort spielbar, sammelbar, trainierbar und tauschbar sind – ohne tote Platzhalter.
- **Entscheidung 2: Interaktives Training mit direkter Evolutions-Kopplung**
  - *Gewählter Ansatz*: Jedes Monster besitzt neben Level und Genetik-Rang (C bis S+) spezifische **Trainings-Fokuswerte (TP)** in Angriff, Verteidigung, Geschwindigkeit, Elementarkraft und Vitalität. Drei interaktive Mini-Spiele im Dojo belohnen Spieler-Geschicklichkeit mit TP-Multiplikatoren und seltenen Trainings-Marken. Bestimmte Evolutionen prüfen das Verhältnis dieser trainierten Werte (z. B. *Angriff > Verteidigung* führt zur *Klingen-Form*, *Verteidigung > Angriff* zur *Bastions-Form*, oder Einsatz eines *Artefakts* zur *Aether-Form*).
  - *Warum*: Verbindet das gewünschte Geschicklichkeits-Training unmittelbar mit dem komplexen Evolutionssystem.
- **Entscheidung 3: Dynamischer Quest-Generator mit Fraktions-Politik**
  - *Gewählter Ansatz*: Ein kontextsensitiver Generator analysiert Spieler-Position (Kontinent & Biom), Spieler-Level, abgeschlossene Quests, lokales Wetter/Tageszeit, vorkommende Monsterarten und den politischen Ruf bei den 3 Fraktionen, um maßgeschneiderte Quests mit transparenter Schwierigkeits- und Belohnungsformel (EP, Gold, seltene Crafting-Erze, Evolutions-Artefakte, Fraktions-Einfluss) zu erzeugen.
  - *Warum*: Sorgt für endlosen Wiederspielwert auf allen 3 Kontinenten und macht Fraktionsentscheidungen spürbar.
- **Entscheidung 4: Cloud-Speicherung, Online-Tausch & PvP-Arena über Firebase Firestore**
  - *Gewählter Ansatz*: Authentifizierung über Google Sign-In (`CredentialManager`) und Speicherung des kompletten Spielerprofils (`/users/{userId}`), der globalen Tauschbörse (`/trades/{tradeId}`) und der Online-Arena-Teams (`/arena_rosters/{userId}`) in Firestore mit gehärteten Sicherheitsregeln.
  - *Warum*: Ermöglicht echten Cross-Device-Fortschritt, sicheren Spieler-zu-Spieler-Kreaturentausch und echte Arena-Herausforderungen.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                   AETHERON: UI & PRESENTATION LAYER (Compose)               │
├──────────────────┬──────────────────┬──────────────────┬────────────────────┤
│   World & Map    │  Tactical Battle │  Dojo Training & │  Quests, Crafting  │
│    Explorer      │     Theater      │   Breeding Lab   │   & Online Arena   │
│ ┌──────────────┐ │ ┌──────────────┐ │ ┌──────────────┐ │ ┌────────────────┐ │
│ │2D Tile Canvas│ │ │Turn Engine UI│ │ │3 Skill Mini- │ │ │Dynamic Quest   │ │
│ │Weather/Time  │ │ │Weather/Trait │ │ │Games (ATK/   │ │ │Generator UI,   │ │
│ │NPC Schedules │ │ │FX & Capture  │ │ │DEF/SPD) &    │ │ │Faction Politics│ │
│ │24 Gym Leaders│ │ │Animations    │ │ │Evolution Tree│ │ │Trade & PvP Hub │ │
│ └──────┬───────┘ │ └──────┬───────┘ │ └──────┬───────┘ │ └────────┬───────┘ │
└────────┼──────────────────┼──────────────────┼────────────────────┼─────────┘
         │                  │                  │                    │
         ▼                  ▼                  ▼                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     DOMAIN & GAMEPLAY ENGINE (ViewModel)                    │
│  • WorldSimulationEngine (Day/Night Cycle, Dynamic Weather, Biome Spawns)   │
│  • TacticalCombatEngine (Type Matrix, Stamina, Genetic Traits, Weather Mod) │
│  • TrainingAndEvolutionEngine (Mini-Game Scoring -> Stat TP -> Branching)   │
│  • BreedingGeneticsEngine (Parent Pairing, IV/Trait Inheritance, Legend+)   │
│  • DynamicQuestAndFactionEngine (Location/Level/Biome/Faction Quest Synth)  │
│  • CraftingAndSkillTreeEngine (Recipes, Artifacts, Player Skill Perks)      │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                DATA & CLOUD PERSISTENCE LAYER (Firebase + Local)            │
├──────────────────────────────────────┬──────────────────────────────────────┤
│   Firebase Auth (CredentialManager)  │   Cloud Firestore (Named Database)   │
│  • Google Sign-In Gate               │  • /users/{uid} (Cloud Save State,   │
│  • Multi-Device Session Sync         │    Badges, Factions, Inventory, Team)│
│                                      │  • /trades/{tradeId} (Live P2P Trade)│
│                                      │  • /arena/{uid} (Online PvP Rosters) │
└──────────────────────────────────────┴──────────────────────────────────────┘
```

### Data Model & State
- **`MonsterSpecies` & `MonsterInstance`**:
  - Spezies-Katalog (ID 1–240, Kontinent 1–3, Biom-Heimat, Wetter-/Tageszeit-Präferenz, Elementartypen, Basis-Stats, 2–3 Evolutionsstufen mit Stat-/Artefakt-Bedingungen).
  - Individuelle Instanz (`uid`, `speciesId`, `level`, `xp`, Basis-Gene `ivRank`, vererbte `geneticTraits`, trainierte Werte `trainedAtk`, `trainedDef`, `trainedSpd`, `trainedElem`, `trainedHp`, `generation` für Zucht-Verstärkung inkl. Legendärer Monster).
- **`WorldState` & `NpcSchedule`**:
  - Aktueller Kontinent (1–3), aktives Biom, Koordinaten, Tageszeit (`DAWN`, `DAY`, `DUSK`, `NIGHT`), dynamisches Wetter (`CLEAR`, `RAIN`, `STORM`, `SANDSTORM`, `AETHER_MIST`), NPC-Tagesroutinen und Status der 24 Arena-Leiter.
- **`DynamicQuest` & `FactionStanding`**:
  - Rufwerte und politischer Einfluss der 3 Fraktionen pro Kontinent; generierte Quests mit `questType` (`HUNT`, `GATHER`, `EXPLORE`, `BOSS_BOUNTY`), Ziel-Biom, Ziel-Monster/Item, Schwierigkeitsgrad (1–5 Sterne) und skaliertem Belohnungspaket.
- **`TradeListing` & `ArenaChallenger` (Firestore)**:
  - Echtzeit-Dokumente für den globalen Monster-Tausch (Angebotenes Monster + Gesuchtes Kriterium/Direkttausch) sowie registrierte Spieler-Kampfteams für den Online-Duell-Modus.

### Interactive Component & State Mapping
- **Welt- & Biom-Navigation**: Bewegung auf dem 2D-Grid aktualisiert Spielerposition, prüft NPC-Sichtlinien (abhängig von deren Uhrzeit-Position) und berechnet Begegnungschancen anhand des aktuellen Wetters und der Tageszeit.
- **Training-Mini-Games -> Statistik & Evolution**:
  - *Kraft-Schmiede (Angriff & Elementarkraft)*: Timing-basiertes Treffen bewegter Fokus-Zonen.
  - *Aegis-Reflex (Verteidigung & Vitalität)*: Abblocken einfliegender Elementar-Projektile aus 4 Richtungen.
  - *Aether-Puls (Geschwindigkeit & Initiative)*: Schnelles Sequenz-Tippen von Runen-Knoten gegen die Zeit.
  - Nach Abschluss berechnet der Handler den Rang (S/A/B/C), erhöht die spezifischen Trainings-Statistiken, vergibt Bonus-Crafting-Items und aktualisiert sofort den prognostizierten Evolutionspfad des Monsters.
- **Dynamischer Quest-Generator**: Fordert der Spieler neue Aufträge an oder wechselt das Biom, evaluiert die Engine `playerLevel`, `currentBiome`, `completedQuestsCount`, `dominantFaction` und `activeWeather`, erzeugt 3 passende Missionen mit Schwierigkeits-Multiplikator und schreibt Fortschritt bei jedem Kampf, Fang oder Sammel-Event automatisch fort.
- **Cloud-Save, Tausch & Online-Arena**: Jede wichtige Aktion (Orden gewonnen, Monster gezüchtet/trainiert, Tausch durchgeführt) wird lokal aktualisiert und automatisch in das authentifizierte Firestore-Dokument des Spielers synchronisiert.
