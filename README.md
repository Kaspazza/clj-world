to start shadow-cljs: npx shadow-cljs watch main

to start tailwind watch files: npx tailwindcss -i ./resources/public/css/styles.css -o ./resources/public/dist/output.css --watch

In server.clj run (start) in repl

App runs on 3000 port
Shadow-cljs build on: http://localhost:9630/build/main

Optimizing tailwind for production:
npx tailwindcss -o build.css --minify
