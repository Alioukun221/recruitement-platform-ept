import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);

  console.log('🔍 Interceptor appelé pour:', req.url);
  console.log('🌐 Platform Browser?', isPlatformBrowser(platformId));

  if (isPlatformBrowser(platformId)) {
    const token = localStorage.getItem('auth_token');
    console.log('🔑 Token trouvé?', token ? 'OUI ✅' : 'NON ❌');
    console.log('🔑 Token:', token?.substring(0, 50) + '...');

    if (token) {
      const clonedReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('✅ Header Authorization ajouté');
      return next(clonedReq);
    }
  }

  console.log('⚠️ Requête envoyée SANS token');
  return next(req);
};
