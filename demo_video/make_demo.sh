#!/bin/zsh
set -e

FONT="/System/Library/Fonts/Supplemental/Arial.ttf"
BOLD="/System/Library/Fonts/Supplemental/Arial Bold.ttf"
OUT="demo_video/words_game_demo_fr.mp4"
VOICE="demo_video/narration_fr.aiff"

mkdir -p /tmp/wordsgame-swift-cache
swift -module-cache-path /tmp/wordsgame-swift-cache demo_video/render_slides.swift
say -v Thomas -r 175 -f demo_video/narration_fr.txt -o "$VOICE"

durations=(7 9 9 11 9 8 9 15)
for i in {1..8}; do
  duration=${durations[$i]}
  ffmpeg -y -loop 1 -t "$duration" -i "demo_video/slides/0${i}.png" \
    -vf "scale=1080:1920,format=yuv420p" -r 30 -c:v libx264 -preset medium -crf 20 \
    "demo_video/slides/0${i}.mp4"
done

printf "file '01.mp4'\nfile '02.mp4'\nfile '03.mp4'\nfile '04.mp4'\nfile '05.mp4'\nfile '06.mp4'\nfile '07.mp4'\nfile '08.mp4'\n" > demo_video/slides/list.txt
ffmpeg -y -f concat -safe 0 -i demo_video/slides/list.txt -i "$VOICE" \
  -map 0:v -map 1:a -c:v copy -c:a aac -b:a 160k -shortest -movflags +faststart "$OUT"

echo "$OUT"
