import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import Components from 'unplugin-vue-components/vite'
import { defineConfig, loadEnv } from 'vite'
// @ts-expect-error -- Vite 插件源码为 JS，未提供 .d.ts
import createAutoTestIdPlugin from './plugins/auto-testid/index.js'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const enableAutoTestId = env.VITE_ENABLE_AUTO_TESTID !== 'false'

  return {
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
      createAutoTestIdPlugin({
        enabled: enableAutoTestId,
        verbose: true,
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
            additionalData: `@use "@/assets/styles/variables" as *;\n@use "@/assets/styles/mixins" as *;\n`,
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
            if (id.includes('node_modules/vue') || id.includes('node_modules/@vue')) {
              return 'vue-vendor'
            }
            if (id.includes('node_modules/element-plus') || id.includes('node_modules/@element-plus')) {
              return 'element-plus-vendor'
            }
            if (id.includes('node_modules/@antv/x6') || id.includes('node_modules/@antv/x6-')) {
              return 'antv-x6-vendor'
            }
            if (id.includes('node_modules/monaco-editor')) {
              return 'monaco-vendor'
            }
            if (id.includes('node_modules/echarts')) {
              return 'echarts-vendor'
            }
            if (id.includes('node_modules/dagre')) {
              return 'antv-x6-vendor'
            }
            if (id.includes('node_modules')) {
              return 'vendors'
            }
          },
        },
      },
      chunkSizeWarningLimit: 1000,
    },
    optimizeDeps: {
      include: ['vue', 'vue-router', 'pinia', 'element-plus', 'echarts/core'],
    },
    test: {
      globals: true,
      environment: 'happy-dom',
      include: ['src/**/*.{test,spec}.{js,ts}'],
    },
  }
})
