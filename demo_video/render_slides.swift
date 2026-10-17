import AppKit

let W = 1080, H = 1920
let out = "demo_video/slides"
try? FileManager.default.createDirectory(atPath: out, withIntermediateDirectories: true)

func color(_ hex: Int, _ alpha: CGFloat = 1) -> NSColor {
    NSColor(red: CGFloat((hex >> 16) & 255)/255, green: CGFloat((hex >> 8) & 255)/255, blue: CGFloat(hex & 255)/255, alpha: alpha)
}
func text(_ s: String, y: CGFloat, size: CGFloat, color c: NSColor, bold: Bool = false) {
    let font = bold ? NSFont.boldSystemFont(ofSize: size) : NSFont.systemFont(ofSize: size)
    let style = NSMutableParagraphStyle(); style.alignment = .center
    let attrs: [NSAttributedString.Key: Any] = [.font: font, .foregroundColor: c, .paragraphStyle: style]
    let box = NSRect(x: 50, y: CGFloat(H) - y - size * 2.0, width: CGFloat(W - 100), height: size * 2.4)
    (s as NSString).draw(in: box, withAttributes: attrs)
}
func rounded(_ rect: NSRect, fill: NSColor, radius: CGFloat = 28) {
    fill.setFill(); NSBezierPath(roundedRect: rect, xRadius: radius, yRadius: radius).fill()
}
func slide(_ n: Int, bg: Int, draw: () -> Void) {
    let image = NSImage(size: NSSize(width: W, height: H))
    image.lockFocus(); color(bg).setFill(); NSRect(x: 0, y: 0, width: W, height: H).fill(); draw(); image.unlockFocus()
    guard let tiff = image.tiffRepresentation, let rep = NSBitmapImageRep(data: tiff),
          let data = rep.representation(using: .png, properties: [:]) else { return }
    try? data.write(to: URL(fileURLWithPath: String(format: "%@/%02d.png", out, n)))
}
func screenshot(_ path: String, number: Int, title: String, lines: [String], accent: Int) {
    slide(number, bg: 0xFFFFFF) {
        if let img = NSImage(contentsOfFile: path) {
            let scale = min(980 / img.size.width, 1420 / img.size.height)
            let size = NSSize(width: img.size.width * scale, height: img.size.height * scale)
            img.draw(in: NSRect(x: (CGFloat(W)-size.width)/2, y: 430, width: size.width, height: size.height))
        }
        rounded(NSRect(x: 0, y: 0, width: W, height: 430), fill: color(accent))
        text(title, y: 1535, size: 48, color: .white, bold: true)
        for (i, line) in lines.enumerated() { text(line, y: 1630 + CGFloat(i*62), size: 34, color: i == 0 ? color(0xFFD54F) : .white) }
    }
}

slide(1, bg: 0x0D47A1) {
    text("WORDS GAME", y: 430, size: 108, color: .white, bold: true)
    text("Le mot quotidien en français et en anglais", y: 590, size: 42, color: color(0xDCEBFF))
    text("DÉMONSTRATION COMPLÈTE", y: 730, size: 40, color: color(0xFFD54F), bold: true)
    let letters = [("M",0x66BB6A),("O",0xFFCA28),("T",0x9E9E9E),("S",0x66BB6A)]
    for (i, item) in letters.enumerated() {
        rounded(NSRect(x: 150 + CGFloat(i*200), y: 700, width: 160, height: 160), fill: color(item.1), radius: 22)
        text(item.0, y: 1010, size: 86, color: .white, bold: true)
    }
}
screenshot("demo_video/frames/01_language.png", number: 2, title: "1. CHOISISSEZ VOTRE LANGUE", lines: ["Français ou English", "Un nouveau mot de 5 lettres chaque jour"], accent: 0x0D47A1)
screenshot("demo_video/frames/02_profile.png", number: 3, title: "2. PERSONNALISEZ VOTRE PROFIL", lines: ["Pseudo enregistré sur l’appareil", "56 avatars disponibles"], accent: 0x4527A0)
slide(4, bg: 0xF4F7FC) {
    text("3. DEVINEZ LE MOT", y: 150, size: 68, color: color(0x0D47A1), bold: true)
    text("6 essais • clavier intégré • chronomètre", y: 260, size: 37, color: color(0x455A64))
    let data = [("M",0x66BB6A),("O",0xFFCA28),("T",0x9E9E9E),("I",0x66BB6A),("F",0x9E9E9E)]
    for (i, item) in data.enumerated() {
        rounded(NSRect(x: 75 + CGFloat(i*195), y: 1190, width: 155, height: 155), fill: color(item.1), radius: 18)
        text(item.0, y: 555, size: 82, color: .white, bold: true)
    }
    text("VERT — bonne lettre, bonne place", y: 810, size: 42, color: color(0x2E7D32), bold: true)
    text("JAUNE — bonne lettre, mauvaise place", y: 970, size: 40, color: color(0xB77900), bold: true)
    text("GRIS — lettre absente du mot", y: 1130, size: 42, color: color(0x616161), bold: true)
    rounded(NSRect(x: 110, y: 230, width: 860, height: 160), fill: color(0xD32F2F))
    text("Mot non reconnu ? L’app vous avertit.", y: 1510, size: 36, color: .white, bold: true)
}
slide(5, bg: 0xEEF5FF) {
    text("4. UNE TENTATIVE BONUS", y: 240, size: 64, color: color(0x0D47A1), bold: true)
    text("Après les 6 essais", y: 370, size: 44, color: color(0x455A64))
    rounded(NSRect(x: 190, y: 760, width: 700, height: 260), fill: color(0x1976D2))
    text("▶  REGARDER LA VIDÉO", y: 820, size: 52, color: .white, bold: true)
    text("Débloquez un essai supplémentaire", y: 1110, size: 40, color: color(0x263238), bold: true)
    text("ou terminez la partie", y: 1190, size: 36, color: color(0x607D8B))
}
slide(6, bg: 0xFFF8E8) {
    text("5. RÉSULTATS ET STATISTIQUES", y: 170, size: 56, color: color(0x8A5A00), bold: true)
    text("VICTOIRE  🎉", y: 330, size: 72, color: color(0x2E7D32), bold: true)
    let stats = [("PARTIES","12",0x1976D2),("VICTOIRES","75%",0x388E3C),("SÉRIE","4",0xF9A825),("RECORD","8",0xEF6C00)]
    for i in 0..<4 {
        let x: CGFloat = i % 2 == 0 ? 80 : 570; let y: CGFloat = i < 2 ? 930 : 570
        rounded(NSRect(x: x, y: y, width: 430, height: 270), fill: .white)
        text(stats[i].0, y: i < 2 ? 680 : 1040, size: 36, color: color(stats[i].2), bold: true)
        text(stats[i].1, y: i < 2 ? 755 : 1115, size: 68, color: color(0x263238), bold: true)
    }
    text("Temps • mot correct • prochain mot", y: 1430, size: 38, color: color(0x455A64))
}
slide(7, bg: 0xF5F0FF) {
    text("6. CLASSEMENT ET PARTAGE", y: 200, size: 58, color: color(0x4527A0), bold: true)
    text("Comparez votre score aux autres joueurs", y: 320, size: 38, color: color(0x263238))
    text("🏆  CLASSEMENT", y: 590, size: 64, color: color(0xB07A00), bold: true)
    text("🟩 🟨 ⬜ 🟩 🟩", y: 850, size: 78, color: .black, bold: true)
    text("🟩 🟩 🟩 🟩 🟩", y: 980, size: 78, color: .black, bold: true)
    text("Partagez votre grille sans révéler le mot", y: 1200, size: 37, color: color(0x263238))
    rounded(NSRect(x: 190, y: 290, width: 700, height: 160), fill: color(0x1976D2))
    text("PARTAGER LE RÉSULTAT", y: 1470, size: 43, color: .white, bold: true)
}
slide(8, bg: 0x0D47A1) {
    text("PRÊT À JOUER ?", y: 500, size: 94, color: .white, bold: true)
    text("Un nouveau défi chaque jour", y: 670, size: 48, color: color(0xDCEBFF))
    text("WORDS GAME", y: 900, size: 76, color: color(0xFFD54F), bold: true)
    text("Bilingue • rapide • personnalisable • compétitif", y: 1040, size: 34, color: .white)
}
