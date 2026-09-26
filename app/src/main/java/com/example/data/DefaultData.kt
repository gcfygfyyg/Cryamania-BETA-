package com.example.data

import com.example.data.model.ChatRoom
import com.example.data.model.ShopItem

object DefaultData {
    // Empty lobbies cannot be seen! Public lobbies only appear when a player actively hosts one.
    val defaultRooms = emptyList<ChatRoom>()

    val defaultShopItems = listOf(
        // 3D Shirts & Outfits (Category: SHIRT)
        ShopItem(
            id = "outfit_cyber_by",
            name = "Black & Yellow Cyber Shirt",
            category = "SHIRT",
            priceCrin = 0,
            iconEmoji = "👕",
            description = "Minimalist pitch-black 3D torso with vibrant yellow sleeves and geometric chest emblem.",
            isUnlocked = true,
            isEquipped = true
        ),
        ShopItem(
            id = "outfit_classic_r",
            name = "Classic 'R' Stud Shirt",
            category = "SHIRT",
            priceCrin = 150,
            iconEmoji = "🟨",
            description = "Authentic 3D blocky shirt with raised yellow 'R' badge and contrasting cuffs.",
            isUnlocked = true,
            isEquipped = false
        ),
        ShopItem(
            id = "outfit_iron_cafe",
            name = "Iron Cafe Barista Apron",
            category = "SHIRT",
            priceCrin = 250,
            iconEmoji = "☕",
            description = "3D layered barista shirt with golden-yellow apron, collar, and front pocket."
        ),
        ShopItem(
            id = "outfit_hazard_hoodie",
            name = "Hazard Stripe 3D Hoodie",
            category = "SHIRT",
            priceCrin = 350,
            iconEmoji = "⚠️",
            description = "Industrial black and yellow diagonal warning stripes with 3D sculpted collar."
        ),
        ShopItem(
            id = "outfit_varsity",
            name = "Varsity Letterman Jacket",
            category = "SHIRT",
            priceCrin = 400,
            iconEmoji = "🧥",
            description = "3D wool-black jacket with bright yellow sleeves, ribbed trim, and 'C' patch."
        ),
        ShopItem(
            id = "outfit_tuxedo",
            name = "VIP Gold & Noir Tuxedo",
            category = "SHIRT",
            priceCrin = 500,
            iconEmoji = "🤵",
            description = "Sharp 3D black tuxedo jacket with yellow-gold silk tie and pocket square."
        ),
        ShopItem(
            id = "outfit_builder_flannel",
            name = "Builder Vest & Suspenders",
            category = "SHIRT",
            priceCrin = 300,
            iconEmoji = "🦺",
            description = "High-visibility yellow safety vest over black 3D work shirt and utility belt."
        ),
        ShopItem(
            id = "outfit_skater_tee",
            name = "Skater Layered Graphic Tee",
            category = "SHIRT",
            priceCrin = 280,
            iconEmoji = "⚡",
            description = "Black graphic tee over yellow long-sleeve thermal with lightning print."
        ),

        // 3D Hats & Headgear (Category: COSMETIC)
        ShopItem(
            id = "hat_crown",
            name = "Dominos Crown",
            category = "COSMETIC",
            priceCrin = 500,
            iconEmoji = "👑",
            description = "3D sculpted golden-yellow crown with obsidian inlays."
        ),
        ShopItem(
            id = "hat_builder",
            name = "Classic Builder Hardhat",
            category = "COSMETIC",
            priceCrin = 250,
            iconEmoji = "⛑️",
            description = "Iconic 3D yellow construction hardhat with black brim and emblem."
        ),
        ShopItem(
            id = "hat_headphones",
            name = "Clockwork Headphones",
            category = "COSMETIC",
            priceCrin = 300,
            iconEmoji = "🎧",
            description = "3D black and yellow studio headphones that pulse when speaking."
        ),
        ShopItem(
            id = "hat_top_hat",
            name = "Classic Top Hat",
            category = "COSMETIC",
            priceCrin = 350,
            iconEmoji = "🎩",
            description = "Tall 3D noir top hat with a bright yellow satin band."
        ),
        ShopItem(
            id = "hat_visor",
            name = "Cyber Hazard Visor",
            category = "COSMETIC",
            priceCrin = 320,
            iconEmoji = "🥽",
            description = "Sleek wrap-around yellow neon visor for your 3D avatar head."
        ),
        ShopItem(
            id = "hat_halo",
            name = "Golden Halo",
            category = "COSMETIC",
            priceCrin = 400,
            iconEmoji = "😇",
            description = "Floating 3D ring of pure yellow light above your avatar."
        ),

        // 3D Auras & Effects (Category: EFFECT)
        ShopItem(
            id = "aura_rainbow",
            name = "Yellow Volt Trail",
            category = "EFFECT",
            priceCrin = 450,
            iconEmoji = "〰️",
            description = "Leave glowing yellow and black kinetic afterimages as you walk."
        ),
        ShopItem(
            id = "aura_sparkles",
            name = "Gold Sparkles",
            category = "EFFECT",
            priceCrin = 200,
            iconEmoji = "✨",
            description = "Orbiting 3D yellow star sparkles floating around your avatar."
        ),
        ShopItem(
            id = "aura_neon_fire",
            name = "Black & Yellow Aura",
            category = "EFFECT",
            priceCrin = 600,
            iconEmoji = "🔥",
            description = "Pulsing dual-ring yellow energy field around your 3D character."
        ),

        // 3D Room Decorations (Category: DECORATION)
        ShopItem(
            id = "deco_dj_booth",
            name = "3D Jukebox Deck",
            category = "DECORATION",
            priceCrin = 700,
            iconEmoji = "📻",
            description = "3D isometric dual-speaker jukebox in black and yellow."
        ),
        ShopItem(
            id = "deco_disco_ball",
            name = "Golden Mirror Sphere",
            category = "DECORATION",
            priceCrin = 400,
            iconEmoji = "🪩",
            description = "Projects rotating yellow light beams across the 3D floor."
        ),
        ShopItem(
            id = "deco_neon_sign",
            name = "Lobby Billboard Sign",
            category = "DECORATION",
            priceCrin = 350,
            iconEmoji = "💡",
            description = "Minimalist black and yellow 3D floating lobby sign."
        ),
        ShopItem(
            id = "deco_arcade",
            name = "3D Retro Arcade Cabinet",
            category = "DECORATION",
            priceCrin = 550,
            iconEmoji = "🕹️",
            description = "Isometric 3D arcade machine with glowing yellow screen."
        ),
        ShopItem(
            id = "deco_lounge_sofa",
            name = "3D Studded Lounge Sofa",
            category = "DECORATION",
            priceCrin = 300,
            iconEmoji = "🛋️",
            description = "3D blocky bench where avatars can sit and hang out."
        )
    )
}
