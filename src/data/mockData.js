export const repositories = [
 {id:1,name:'secure-auth-service',owner:'yeswanth-reddy',branch:'main',language:'Java',status:'Completed',lastScan:'2 min ago',scans:12},
 {id:2,name:'payment-gateway-api',owner:'yeswanth-reddy',branch:'develop',language:'JavaScript',status:'Running',lastScan:'Scanning now',scans:8},
 {id:3,name:'customer-portal',owner:'team-secure',branch:'main',language:'TypeScript',status:'Completed',lastScan:'Yesterday',scans:5}
];
export const findings = [
 {id:1,severity:'Critical',title:'Hardcoded credential detected',rule:'generic.secrets.security.detected-aws-access-key',file:'src/config/AwsConfig.java',line:42,cwe:'CWE-798',description:'A credential-like value was detected directly in source code.',explanation:'Secrets embedded in code can be exposed through repository history, forks, or build artifacts. An attacker may use the credential to access protected resources.',impact:'Unauthorized access to cloud resources and potential data exposure.',fix:'Move secrets to a managed secret store or environment variables, rotate the exposed credential, and remove it from repository history.'},
 {id:2,severity:'High',title:'Potential SQL injection',rule:'java.lang.security.audit.jdbc-sqli',file:'src/repository/UserRepository.java',line:88,cwe:'CWE-89',description:'A query appears to be constructed using an untrusted input.',explanation:'If user-controlled data is concatenated into SQL, an attacker may alter the query structure and access or modify data outside the intended scope.',impact:'Data disclosure, unauthorized data modification, or authentication bypass.',fix:'Use parameterized queries or prepared statements. Validate inputs and avoid building SQL with string concatenation.'},
 {id:3,severity:'Medium',title:'Weak cryptographic algorithm',rule:'java.security.audit.crypto-weak-hash',file:'src/security/TokenUtil.java',line:27,cwe:'CWE-328',description:'A weak hash algorithm is used in a security-sensitive context.',explanation:'Legacy algorithms may not provide sufficient collision resistance for security-sensitive operations.',impact:'Integrity checks or security tokens may be easier to compromise.',fix:'Use a modern cryptographic algorithm appropriate to the use case, such as SHA-256 or a standard password hashing function.'},
 {id:4,severity:'Low',title:'Debug logging may expose information',rule:'java.lang.security.audit.debug-logging',file:'src/controller/AuthController.java',line:61,cwe:'CWE-532',description:'Debug logging includes potentially sensitive details.',explanation:'Logs may be retained or accessed by more people and systems than the application data itself.',impact:'Sensitive operational or user information could be disclosed through logs.',fix:'Remove sensitive values from logs and use structured, redacted logging.'}
];
export const scans = [
 {id:41,repo:'secure-auth-service',branch:'main',date:'Nov 03, 2026 · 10:42 AM',status:'Completed',findings:4,duration:'1m 24s'},
 {id:40,repo:'payment-gateway-api',branch:'develop',date:'Nov 02, 2026 · 04:18 PM',status:'Completed',findings:7,duration:'2m 06s'},
 {id:39,repo:'customer-portal',branch:'main',date:'Nov 01, 2026 · 11:03 AM',status:'Failed',findings:0,duration:'—'},
 {id:38,repo:'secure-auth-service',branch:'main',date:'Oct 30, 2026 · 09:15 AM',status:'Completed',findings:2,duration:'1m 11s'}
];
