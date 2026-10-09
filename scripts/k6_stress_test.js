import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        ramping_tps: {
            executor: 'ramping-arrival-rate',
            startRate: 0,
            timeUnit: '1s',
            preAllocatedVUs: 100,
            maxVUs: 1000,
            stages: [
                { target: 100, duration: '1m' }, // 0 to 100 TPS in 1m
                { target: 200, duration: '1m' }, // 100 to 200 TPS in 1m
                { target: 300, duration: '1m' }, // 200 to 300 TPS in 1m
                { target: 400, duration: '1m' }, // 300 to 400 TPS in 1m
                { target: 500, duration: '1m' }, // 400 to 500 TPS in 1m
                { target: 600, duration: '1m' }, // 500 to 600 TPS in 1m
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<50'], // P95 < 50ms
        http_req_failed: ['rate<0.01'],   // Error rate < 1%
    },
};

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8081'; // WAS 포트 8081 반영
const SHORT_KEY = __ENV.SHORT_KEY || 'FX8mnGX';

export default function () {
    const res = http.get(`${BASE_URL}/r/${SHORT_KEY}`, {
        redirects: 0,
    });

    check(res, {
        'is status 302': (r) => r.status === 302,
        'has location header': (r) => r.headers['Location'] !== undefined,
    });
}
