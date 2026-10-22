# Security Specification — Artmuseum

## 1. Data Invariants
1. **Authentication Requirement**: Every read and write operation across all collections requires `request.auth != null`.
2. **User Profile Ownership (`/users/{userId}`)**: Only `request.auth.uid == userId` can create, update, or delete `/users/{userId}`. Reads require either `request.auth.uid == userId` or `resource.data.profileVisibility == 'public'`. No PII (email, phone, address) is stored in user profile documents.
3. **Post Integrity (`/posts/{postId}`)**: `authorId` must equal `request.auth.uid` on creation and is immutable on update. Only the author can edit post content or delete the post; non-authors can only increment/decrement `likeCount` or `commentCount` by at most 1 on active public posts. List queries must filter by `visibility == 'public'` or `authorId == request.auth.uid`.
4. **Like Idempotency (`/posts/{postId}/likes/{likeUid}`)**: Document ID `likeUid` must equal `request.auth.uid` and `incoming().uid == request.auth.uid`. Parent post `/posts/{postId}` must exist on creation.
5. **Comment Ownership (`/posts/{postId}/comments/{commentId}`)**: `authorId` must equal `request.auth.uid` on creation, parent post `/posts/{postId}` must exist, and only the comment author can update or delete their comment.
6. **Saved Posts Isolation (`/users/{userId}/savedPosts/{postId}`)**: Strictly private to `request.auth.uid == userId`.
7. **Notifications Isolation (`/users/{userId}/notifications/{notificationId}`)**: Only `request.auth.uid == userId` can read, mark read, or delete notifications; creation requires `actorId == request.auth.uid`.
8. **Blocks & Reports (`/users/{userId}/blocks/{blockedUid}`, `/reports/{reportId}`)**: Blocks are strictly isolated to `request.auth.uid == userId`. Reports require `reporterUid == request.auth.uid` and are immutable once submitted.

## 2. The Dirty Dozen Payloads
1. **Unauthenticated Read/Write**: `unauthDb.collection("posts").where("visibility", "==", "public").get()` -> Reject (`PERMISSION_DENIED`).
2. **Unfiltered Collection Scraping**: `aliceDb.collection("posts").get()` without `visibility == 'public'` or `authorId == uid` -> Reject (`PERMISSION_DENIED`).
3. **Identity Spoofing on Post Create**: Alice creates a post with `authorId: "bob_456"` -> Reject.
4. **Shadow Field Injection**: Alice creates or updates a profile with `isAdmin: true` -> Reject via `hasOnly()`.
5. **Author Mutation on Post Update**: Alice updates `authorId` on her post -> Reject (`incoming().authorId == existing().authorId`).
6. **CreatedAt Tampering**: Alice modifies `createdAt` during profile or post update -> Reject (`incoming().createdAt == existing().createdAt`).
7. **Cross-User Saved Post Read**: Bob reads `/users/alice_123/savedPosts/post_1` -> Reject.
8. **Cross-User Like Impersonation**: Alice writes `/posts/post_1/likes/bob_456` -> Reject.
9. **Orphaned Comment Creation**: Alice creates `/posts/non_existent_post/comments/c_1` -> Reject via `exists()` parent check.
10. **Arbitrary Like Count Inflation**: Bob updates `likeCount` on Alice's post from `0` to `9999` -> Reject (delta bounded to +/-1).
11. **Value Poisoning / Oversized Strings**: Alice writes a 1MB bio or invalid gender `"Unknown"` -> Reject via `isValidUserProfile()`.
12. **Cross-User Notification Read**: Bob queries `/users/alice_123/notifications` -> Reject.
