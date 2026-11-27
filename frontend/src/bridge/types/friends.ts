export type FriendshipStatus = "PENDING" | "ACCEPTED";

export interface FriendResponse {
    id: number;
    username: string;
    since: string;
}

export interface FriendRequestResponse {
    id: number;
    username: string;
    status: FriendshipStatus;
}

export interface PendingRequest {
    id: number;
    username: string;
    sent_at: string;
}

export interface PendingRequestsResponse {
    incoming: PendingRequest[];
    outgoing: PendingRequest[];
}
