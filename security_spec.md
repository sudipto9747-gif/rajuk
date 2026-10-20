# Security Specification: Office Monthly Report (ইমারত পরিদর্শন প্রতিবেদন ও অ্যাডমিন অনুমোদন সিস্টেম)

## 1. Data Invariants
1. **Strict Multi-User Isolation**: Every document in `/users/{userId}` and `/users/{userId}/inspections/{inspectionId}` belongs exclusively to `userId == request.auth.uid`. Cross-user reads, lists, writes, or deletes are strictly prohibited.
2. **Phone & Password Registration with Admin Approval (`/user_accounts/{userId}`)**:
   - Regular users (`!isAdmin()`) can only register their own account (`request.auth.uid == userId`) with `approvalStatus == 'pending'` and `role == 'inspector'`.
   - Regular users can NEVER self-approve (`approvalStatus == 'approved'`) or escalate privileges (`role == 'admin'`).
   - Only a verified Admin (`isAdmin()`, verified via `/admins/{uid}` or `request.auth.token.email == 'sudipto9747@gmail.com' && request.auth.token.email_verified == true`) can update `approvalStatus` to `'approved'` or `'rejected'`.
3. **Email Spoofing Guard**: Any check on `request.auth.token.email` strictly requires `request.auth.token.email_verified == true`.
4. **Temporal & Identity Integrity**: `createdAt` and `updatedAt` must be server timestamps (`is timestamp && <= request.time`). `userId` and `createdAt` are immutable on update.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read**: `GET /users/alice_123/inspections/rep_1` with `auth = null` -> REJECT.
2. **Cross-User Read (PII Leak)**: `GET /user_accounts/bob_456` as non-admin `alice_123` -> REJECT.
3. **Self-Approved Registration Attack**: `CREATE /user_accounts/alice_123` by non-admin with `approvalStatus: "approved"` -> REJECT.
4. **Self-Assigned Admin Role Attack**: `CREATE /user_accounts/alice_123` by non-admin with `role: "admin"` -> REJECT.
5. **Privilege Escalation Update Attack**: `UPDATE /user_accounts/alice_123` by non-admin setting `approvalStatus: "approved"` -> REJECT.
6. **Admin Email Spoofing Attack**: `UPDATE /user_accounts/alice_123` by user with `email: "sudipto9747@gmail.com"` but `email_verified: false` -> REJECT.
7. **Shadow Update (Ghost Field)**: `UPDATE /users/alice_123/inspections/rep_1` adding `isVerified: true` -> REJECT via `hasOnly()`.
8. **Immutable Field Tampering (`createdAt`)**: `UPDATE /user_accounts/alice_123` mutating `createdAt` -> REJECT.
9. **Resource Exhaustion (1MB String)**: `CREATE` with `buildingAddress` of 5000 chars (> 300 limit) -> REJECT.
10. **Negative Inspection Count**: `CREATE` with `previousInspectionCount: -1` -> REJECT.
11. **Invalid Password Hash Format**: `CREATE /user_accounts/alice_123` with raw plaintext password instead of 64-char hex SHA-256 hash -> REJECT.
12. **Future Timestamp Spoofing**: `CREATE` with `createdAt` in the future (`> request.time`) -> REJECT.
