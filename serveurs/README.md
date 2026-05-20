# Serveur de Synchronisation StaffFlow (Backend dans `serveurs`)

Ce serveur Node.js léger gère de manière centralisée les données de l'application **StaffFlow** (employés, pointages de présence, et fiches de paie). L'application s'y connecte de manière transparente sur le même réseau WiFi local, tout en restant autonome et fonctionnelle hors ligne à 100%.

## Prérequis

- **Node.js** installé (téléchargeable sur [https://nodejs.org/](https://nodejs.org/))

## Comment l'utiliser sur votre PC (Serveur global)

1. **Copier le dossier `serveurs`** du projet sur votre PC.
2. **Ouvrir un terminal (Invite de commandes)** dans ce dossier.
3. Installez les packages requis en tapant la commande :
   ```bash
   npm install
   ```
4. Lancez le serveur central :
   ```bash
   npm start
   ```

Le serveur fonctionnera sur le port **3000** : `http://0.0.0.0:3000` ou `http://localhost:3000`.

## Trouver l'adresse IP de votre PC pour l'application Android

Pour que l'application de votre téléphone se connecte au serveur de votre PC, vous devez entrer l'IP locale de votre PC dans l'UI de l'application :
- **Sous Windows** : Ouvrez un terminal et tapez `ipconfig`. Recherchez "Adresse IPv4" (généralement quelque chose comme `192.168.1.XX` ou `10.0.0.XX`).
- **Sous macOS / Linux** : Ouvrez un terminal et tapez `ip a` ou `ifconfig`.

Dans l'application Android StaffFlow (dans **"Administration > Synchronisation PC / Serveur Central"**), entrez l'adresse URL correspondante (ex: `http://192.168.1.50:3000`) et cliquez sur **Synchroniser Maintenant** !
