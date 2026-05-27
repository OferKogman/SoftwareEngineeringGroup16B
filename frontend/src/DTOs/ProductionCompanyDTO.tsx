export type ProductionCompanyDTO = {
    productionCompanyID: number;
    name: string;
    rating: number;
    founderID: string;
    members: string[];
    invites: string[];
    childrenByUser: string[];
}