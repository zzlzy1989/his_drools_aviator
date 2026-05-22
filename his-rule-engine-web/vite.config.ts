import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/assets/styles/variables" as *;\n`,
      },
    },
  },
  server: {
    port: 8999,
    proxy: {
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: (id) => {
          // Vue 核心单独 chunk
          if (id.includes('node_modules/vue')) {
            return 'vue-vendor'
          }
          // Element Plus 单独 chunk
          if (id.includes('element-plus')) {
            return 'element-plus-vendor'
          }
          // AntV X6 单独 chunk
          if (id.includes('@antv/x6') || id.includes('@antv/x6-vue-shape')) {
            return 'antv-x6-vendor'
          }
          // Monaco Editor 单独 chunk
          if (id.includes('monaco-editor')) {
            return 'monaco-vendor'
          }
          // 其他 node_modules 打包为 vendors
          if (id.includes('node_modules')) {
            return 'vendors'
          }
        },
      },
    },
    chunkSizeWarningLimit: 1000, // 提高阈值避免警告
  },
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'element-plus'],
  },
  test: {
    globals: true,
    environment: 'happy-dom',
    include: ['src/**/*.{test,spec}.{js,ts}'],
  },
})
