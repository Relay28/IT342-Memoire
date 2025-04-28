import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import tailwindcss from '@tailwindcss/vite'
import rollupNodePolyFill from 'rollup-plugin-node-polyfills'
import compression from 'vite-plugin-compression';

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    compression({
      algorithm: 'gzip',
      threshold: 10240,
      deleteOriginFile: false, // Keep original files
      filter: /\.(js|css|html)$/i,
      filename: '[path][base].gz' // Fixes path issues
    }),
  ],
  base: process.env.VITE_BASE_PATH || "/",
  server: {
    headers: {
      'Service-Worker-Allowed': '/'
    }
  },
  define: {
    global: 'globalThis',
    'process.env': {},
  },
  optimizeDeps: {
    include: ['buffer'], exclude: ['@firebase/util'] 
  },
  build: {
    rollupOptions: {
      plugins: [rollupNodePolyFill(), ],
      rollupOptions: {
        manualChunks: (id) => {
          if (id.includes('node_modules')) {
            // Split Firebase
            if (id.includes('@firebase') || id.includes('firebase')) 
              return 'vendor-firebase';
            // Split MUI
            if (id.includes('@mui')) return 'vendor-mui';
            return 'vendor';
          }
          // Split routes
          if (id.includes('src/pages/')) {
            return id.split('pages/')[1].split('/')[0];
          }
        }
      },
      chunkSizeWarningLimit: 1000 // Adjust warning threshold
    },
    outDir: "dist",
    assetsDir: "assets",
    sourcemap: false,          // Disable sourcemaps
    chunkSizeWarningLimit: 500 // Raise size warning threshold
  },
})
