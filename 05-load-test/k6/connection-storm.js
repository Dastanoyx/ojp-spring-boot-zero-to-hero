// k6 load test: simulate a connection storm against the Book API.
//
// Run against the DIRECT-JDBC app and the OJP app and compare. The interesting
// signal is what happens at the DATABASE (pg_stat_activity connection count and
// error rate), not just request latency.
//
//   k6 run -e BASE_URL=http://localhost:8080 k6/connection-storm.js
//
// Ramps to a high number of virtual users quickly to mimic an autoscaling spike.

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Trend('app_error_rate');
const dbErrors = new Rate('db_connection_errors');

export const options = {
  scenarios: {
    connection_storm: {
      executor: 'ramping-vus',
      startVUs: 5,
      stages: [
        { duration: '20s', target: 50 },   // spike up fast
        { duration: '40s', target: 200 },  // sustained heavy load
        { duration: '20s', target: 5 },    // scale back down
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],          // <5% failures is the target
    http_req_duration: ['p(95)<1500'],       // 95th percentile under 1.5s
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // Mixed read/write workload
  const writeRes = http.post(
    `${BASE}/books`,
    JSON.stringify({ title: `t-${__VU}-${__ITER}`, author: 'load' }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  const ok = check(writeRes, {
    'write status 200': (r) => r.status === 200,
  });
  if (!ok) {
    dbErrors.add(1);
    // Connection-refused / too-many-clients show up as 500s here
  } else {
    dbErrors.add(0);
  }

  const readRes = http.get(`${BASE}/books`);
  check(readRes, { 'read status 200': (r) => r.status === 200 });
  errorRate.add(readRes.status >= 400 ? 1 : 0);

  sleep(0.2);
}
