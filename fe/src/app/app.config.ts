import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

// 1. Import các thư viện HTTP cần thiết
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { tokenInterceptor } from './core/interceptors/token.interceptor';

import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';
import { registerLocaleData } from '@angular/common';
import localeVi from '@angular/common/locales/vi';

registerLocaleData(localeVi);
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    
    // 2. QUAN TRỌNG: Cung cấp HttpClient tại đây
    provideHttpClient(
      withFetch(), // Cần thiết cho SSR (Server Side Rendering)
      withInterceptors([tokenInterceptor]) // Gắn Interceptor tự động thêm Token
    ),

    provideClientHydration(withEventReplay()),
    provideAnimations(), // Cần cho Toastr
    provideToastr({ 
      timeOut: 3000, 
      positionClass: 'toast-top-right', 
      preventDuplicates: true 
    })
  ]
};