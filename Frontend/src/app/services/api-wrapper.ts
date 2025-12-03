import { Injectable } from '@angular/core';
import { Api } from '../api/api';
import { HttpContext, HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiWrapperService {
  constructor(private api: Api) {}

  async invokeWithJson<P, R>(
    fn: any,
    params?: P,
    context?: HttpContext
  ): Promise<R> {
    try {
      const response = await this.api.invoke$Response(fn, params, context);
      const body = response.body;

      console.log('Type de body:', typeof body, body); // ✅ Debug

      // Si c'est un Blob, on le convertit en JSON
      if (body instanceof Blob) {
        const text = await body.text();

        console.log('Texte du Blob:', text); // ✅ Debug

        // Vérifier que ce n'est pas vide
        if (!text || text.trim() === '') {
          console.warn('Réponse vide du serveur');
          return [] as unknown as R;
        }

        // Vérifier que ce n'est pas déjà un objet stringifié bizarrement
        if (text.startsWith('[object')) {
          console.error('Le texte reçu n\'est pas du JSON valide:', text);
          throw new Error('Format de réponse invalide');
        }

        try {
          return JSON.parse(text) as R;
        } catch (parseError) {
          console.error('Erreur de parsing JSON:', text);
          throw new Error('Format de réponse invalide');
        }
      }

      // Si c'est déjà un objet JavaScript, on le retourne directement
      if (typeof body === 'object' && body !== null) {
        return body as R;
      }

      // Sinon, on retourne tel quel
      return body as R;
    } catch (error) {
      console.error('Erreur dans invokeWithJson:', error);

      if (error instanceof HttpErrorResponse) {
        console.error('Erreur HTTP:', error.status, error.message);
        throw new Error(`Erreur ${error.status}: ${error.message}`);
      }

      throw error;
    }
  }
}
