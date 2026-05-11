/**
 * Base URL for HTTP API calls.
 * Use empty string so requests stay same-origin (e.g. http://localhost:4200/api/...).
 * `ng serve` proxies /api to the backend (see src/proxy.conf.json).
 * Note: `''` is falsy in JS — do not write `API_BASE_URL || API_FALLBACK_ORIGIN` or you will bypass the proxy and omit JWT on /follow, /saved-post, etc.
 */
import { environment } from '../../environments/environment';

export const API_BASE_URL = '';
export const API_FALLBACK_ORIGIN = (environment.apiUrl !== undefined && environment.apiUrl !== null) ? environment.apiUrl : 'http://localhost:9091';
