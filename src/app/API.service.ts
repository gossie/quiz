/* tslint:disable */
/* eslint-disable */
//  This file was automatically generated and should not be edited.
import { Injectable } from "@angular/core";
import API, { graphqlOperation, GraphQLResult } from "@aws-amplify/api-graphql";
import { Observable } from "zen-observable-ts";

export interface SubscriptionResponse<T> {
  value: GraphQLResult<T>;
}

export type __SubscriptionContainer = {
  onCreateEvent: OnCreateEventSubscription;
  onUpdateEvent: OnUpdateEventSubscription;
  onDeleteEvent: OnDeleteEventSubscription;
};

export type CreateEventInput = {
  id?: string | null;
  aggregateId?: string | null;
  type?: string | null;
  sequenceNumber?: number | null;
  createdAt?: string | null;
  domainEvent?: string | null;
  _version?: number | null;
};

export type ModelEventConditionInput = {
  aggregateId?: ModelIDInput | null;
  type?: ModelStringInput | null;
  sequenceNumber?: ModelIntInput | null;
  createdAt?: ModelStringInput | null;
  domainEvent?: ModelStringInput | null;
  and?: Array<ModelEventConditionInput | null> | null;
  or?: Array<ModelEventConditionInput | null> | null;
  not?: ModelEventConditionInput | null;
};

export type ModelIDInput = {
  ne?: string | null;
  eq?: string | null;
  le?: string | null;
  lt?: string | null;
  ge?: string | null;
  gt?: string | null;
  contains?: string | null;
  notContains?: string | null;
  between?: Array<string | null> | null;
  beginsWith?: string | null;
  attributeExists?: boolean | null;
  attributeType?: ModelAttributeTypes | null;
  size?: ModelSizeInput | null;
};

export enum ModelAttributeTypes {
  binary = "binary",
  binarySet = "binarySet",
  bool = "bool",
  list = "list",
  map = "map",
  number = "number",
  numberSet = "numberSet",
  string = "string",
  stringSet = "stringSet",
  _null = "_null"
}

export type ModelSizeInput = {
  ne?: number | null;
  eq?: number | null;
  le?: number | null;
  lt?: number | null;
  ge?: number | null;
  gt?: number | null;
  between?: Array<number | null> | null;
};

export type ModelStringInput = {
  ne?: string | null;
  eq?: string | null;
  le?: string | null;
  lt?: string | null;
  ge?: string | null;
  gt?: string | null;
  contains?: string | null;
  notContains?: string | null;
  between?: Array<string | null> | null;
  beginsWith?: string | null;
  attributeExists?: boolean | null;
  attributeType?: ModelAttributeTypes | null;
  size?: ModelSizeInput | null;
};

export type ModelIntInput = {
  ne?: number | null;
  eq?: number | null;
  le?: number | null;
  lt?: number | null;
  ge?: number | null;
  gt?: number | null;
  between?: Array<number | null> | null;
  attributeExists?: boolean | null;
  attributeType?: ModelAttributeTypes | null;
};

export type Event = {
  __typename: "Event";
  id: string;
  aggregateId?: string | null;
  type?: string | null;
  sequenceNumber?: number | null;
  createdAt?: string | null;
  domainEvent?: string | null;
  updatedAt: string;
  _version: number;
  _deleted?: boolean | null;
  _lastChangedAt: number;
};

export type UpdateEventInput = {
  id: string;
  aggregateId?: string | null;
  type?: string | null;
  sequenceNumber?: number | null;
  createdAt?: string | null;
  domainEvent?: string | null;
  _version?: number | null;
};

export type DeleteEventInput = {
  id: string;
  _version?: number | null;
};

export type ModelEventFilterInput = {
  id?: ModelIDInput | null;
  aggregateId?: ModelIDInput | null;
  type?: ModelStringInput | null;
  sequenceNumber?: ModelIntInput | null;
  createdAt?: ModelStringInput | null;
  domainEvent?: ModelStringInput | null;
  and?: Array<ModelEventFilterInput | null> | null;
  or?: Array<ModelEventFilterInput | null> | null;
  not?: ModelEventFilterInput | null;
};

export enum ModelSortDirection {
  ASC = "ASC",
  DESC = "DESC"
}

export type ModelEventConnection = {
  __typename: "ModelEventConnection";
  items: Array<Event | null>;
  nextToken?: string | null;
  startedAt?: number | null;
};

export type ModelStringKeyConditionInput = {
  eq?: string | null;
  le?: string | null;
  lt?: string | null;
  ge?: string | null;
  gt?: string | null;
  between?: Array<string | null> | null;
  beginsWith?: string | null;
};

export type CreateEventMutation = {
  __typename: "Event";
  id: string;
  aggregateId?: string | null;
  type?: string | null;
  sequenceNumber?: number | null;
  createdAt?: string | null;
  domainEvent?: string | null;
  updatedAt: string;
  _version: number;
  _deleted?: boolean | null;
  _lastChangedAt: number;
};

export type UpdateEventMutation = {
  __typename: "Event";
  id: string;
  aggregateId?: string | null;
  type?: string | null;
  sequenceNumber?: number | null;
  createdAt?: string | null;
  domainEvent?: string | null;
  updatedAt: string;
  _version: number;
  _deleted?: boolean | null;
  _lastChangedAt: number;
};

export type DeleteEventMutation = {
  __typename: "Event";
  id: string;
  aggregateId?: string | null;
  type?: string | null;
  sequenceNumber?: number | null;
  createdAt?: string | null;
  domainEvent?: string | null;
  updatedAt: string;
  _version: number;
  _deleted?: boolean | null;
  _lastChangedAt: number;
};

export type GetEventQuery = {
  __typename: "Event";
  id: string;
  aggregateId?: string | null;
  type?: string | null;
  sequenceNumber?: number | null;
  createdAt?: string | null;
  domainEvent?: string | null;
  updatedAt: string;
  _version: number;
  _deleted?: boolean | null;
  _lastChangedAt: number;
};

export type ListEventsQuery = {
  __typename: "ModelEventConnection";
  items: Array<{
    __typename: "Event";
    id: string;
    aggregateId?: string | null;
    type?: string | null;
    sequenceNumber?: number | null;
    createdAt?: string | null;
    domainEvent?: string | null;
    updatedAt: string;
    _version: number;
    _deleted?: boolean | null;
    _lastChangedAt: number;
  } | null>;
  nextToken?: string | null;
  startedAt?: number | null;
};

export type SyncEventsQuery = {
  __typename: "ModelEventConnection";
  items: Array<{
    __typename: "Event";
    id: string;
    aggregateId?: string | null;
    type?: string | null;
    sequenceNumber?: number | null;
    createdAt?: string | null;
    domainEvent?: string | null;
    updatedAt: string;
    _version: number;
    _deleted?: boolean | null;
    _lastChangedAt: number;
  } | null>;
  nextToken?: string | null;
  startedAt?: number | null;
};

export type EventsByAggregateIdQuery = {
  __typename: "ModelEventConnection";
  items: Array<{
    __typename: "Event";
    id: string;
    aggregateId?: string | null;
    type?: string | null;
    sequenceNumber?: number | null;
    createdAt?: string | null;
    domainEvent?: string | null;
    updatedAt: string;
    _version: number;
    _deleted?: boolean | null;
    _lastChangedAt: number;
  } | null>;
  nextToken?: string | null;
  startedAt?: number | null;
};

export type OnCreateEventSubscription = {
  __typename: "Event";
  id: string;
  aggregateId?: string | null;
  type?: string | null;
  sequenceNumber?: number | null;
  createdAt?: string | null;
  domainEvent?: string | null;
  updatedAt: string;
  _version: number;
  _deleted?: boolean | null;
  _lastChangedAt: number;
};

export type OnUpdateEventSubscription = {
  __typename: "Event";
  id: string;
  aggregateId?: string | null;
  type?: string | null;
  sequenceNumber?: number | null;
  createdAt?: string | null;
  domainEvent?: string | null;
  updatedAt: string;
  _version: number;
  _deleted?: boolean | null;
  _lastChangedAt: number;
};

export type OnDeleteEventSubscription = {
  __typename: "Event";
  id: string;
  aggregateId?: string | null;
  type?: string | null;
  sequenceNumber?: number | null;
  createdAt?: string | null;
  domainEvent?: string | null;
  updatedAt: string;
  _version: number;
  _deleted?: boolean | null;
  _lastChangedAt: number;
};

@Injectable({
  providedIn: "root"
})
export class APIService {
  async CreateEvent(
    input: CreateEventInput,
    condition?: ModelEventConditionInput
  ): Promise<CreateEventMutation> {
    const statement = `mutation CreateEvent($input: CreateEventInput!, $condition: ModelEventConditionInput) {
        createEvent(input: $input, condition: $condition) {
          __typename
          id
          aggregateId
          type
          sequenceNumber
          createdAt
          domainEvent
          updatedAt
          _version
          _deleted
          _lastChangedAt
        }
      }`;
    const gqlAPIServiceArguments: any = {
      input
    };
    if (condition) {
      gqlAPIServiceArguments.condition = condition;
    }
    const response = (await API.graphql(
      graphqlOperation(statement, gqlAPIServiceArguments)
    )) as any;
    return <CreateEventMutation>response.data.createEvent;
  }
  async UpdateEvent(
    input: UpdateEventInput,
    condition?: ModelEventConditionInput
  ): Promise<UpdateEventMutation> {
    const statement = `mutation UpdateEvent($input: UpdateEventInput!, $condition: ModelEventConditionInput) {
        updateEvent(input: $input, condition: $condition) {
          __typename
          id
          aggregateId
          type
          sequenceNumber
          createdAt
          domainEvent
          updatedAt
          _version
          _deleted
          _lastChangedAt
        }
      }`;
    const gqlAPIServiceArguments: any = {
      input
    };
    if (condition) {
      gqlAPIServiceArguments.condition = condition;
    }
    const response = (await API.graphql(
      graphqlOperation(statement, gqlAPIServiceArguments)
    )) as any;
    return <UpdateEventMutation>response.data.updateEvent;
  }
  async DeleteEvent(
    input: DeleteEventInput,
    condition?: ModelEventConditionInput
  ): Promise<DeleteEventMutation> {
    const statement = `mutation DeleteEvent($input: DeleteEventInput!, $condition: ModelEventConditionInput) {
        deleteEvent(input: $input, condition: $condition) {
          __typename
          id
          aggregateId
          type
          sequenceNumber
          createdAt
          domainEvent
          updatedAt
          _version
          _deleted
          _lastChangedAt
        }
      }`;
    const gqlAPIServiceArguments: any = {
      input
    };
    if (condition) {
      gqlAPIServiceArguments.condition = condition;
    }
    const response = (await API.graphql(
      graphqlOperation(statement, gqlAPIServiceArguments)
    )) as any;
    return <DeleteEventMutation>response.data.deleteEvent;
  }
  async GetEvent(id: string): Promise<GetEventQuery> {
    const statement = `query GetEvent($id: ID!) {
        getEvent(id: $id) {
          __typename
          id
          aggregateId
          type
          sequenceNumber
          createdAt
          domainEvent
          updatedAt
          _version
          _deleted
          _lastChangedAt
        }
      }`;
    const gqlAPIServiceArguments: any = {
      id
    };
    const response = (await API.graphql(
      graphqlOperation(statement, gqlAPIServiceArguments)
    )) as any;
    return <GetEventQuery>response.data.getEvent;
  }
  async ListEvents(
    id?: string,
    filter?: ModelEventFilterInput,
    limit?: number,
    nextToken?: string,
    sortDirection?: ModelSortDirection
  ): Promise<ListEventsQuery> {
    const statement = `query ListEvents($id: ID, $filter: ModelEventFilterInput, $limit: Int, $nextToken: String, $sortDirection: ModelSortDirection) {
        listEvents(id: $id, filter: $filter, limit: $limit, nextToken: $nextToken, sortDirection: $sortDirection) {
          __typename
          items {
            __typename
            id
            aggregateId
            type
            sequenceNumber
            createdAt
            domainEvent
            updatedAt
            _version
            _deleted
            _lastChangedAt
          }
          nextToken
          startedAt
        }
      }`;
    const gqlAPIServiceArguments: any = {};
    if (id) {
      gqlAPIServiceArguments.id = id;
    }
    if (filter) {
      gqlAPIServiceArguments.filter = filter;
    }
    if (limit) {
      gqlAPIServiceArguments.limit = limit;
    }
    if (nextToken) {
      gqlAPIServiceArguments.nextToken = nextToken;
    }
    if (sortDirection) {
      gqlAPIServiceArguments.sortDirection = sortDirection;
    }
    const response = (await API.graphql(
      graphqlOperation(statement, gqlAPIServiceArguments)
    )) as any;
    return <ListEventsQuery>response.data.listEvents;
  }
  async SyncEvents(
    filter?: ModelEventFilterInput,
    limit?: number,
    nextToken?: string,
    lastSync?: number
  ): Promise<SyncEventsQuery> {
    const statement = `query SyncEvents($filter: ModelEventFilterInput, $limit: Int, $nextToken: String, $lastSync: AWSTimestamp) {
        syncEvents(filter: $filter, limit: $limit, nextToken: $nextToken, lastSync: $lastSync) {
          __typename
          items {
            __typename
            id
            aggregateId
            type
            sequenceNumber
            createdAt
            domainEvent
            updatedAt
            _version
            _deleted
            _lastChangedAt
          }
          nextToken
          startedAt
        }
      }`;
    const gqlAPIServiceArguments: any = {};
    if (filter) {
      gqlAPIServiceArguments.filter = filter;
    }
    if (limit) {
      gqlAPIServiceArguments.limit = limit;
    }
    if (nextToken) {
      gqlAPIServiceArguments.nextToken = nextToken;
    }
    if (lastSync) {
      gqlAPIServiceArguments.lastSync = lastSync;
    }
    const response = (await API.graphql(
      graphqlOperation(statement, gqlAPIServiceArguments)
    )) as any;
    return <SyncEventsQuery>response.data.syncEvents;
  }
  async EventsByAggregateId(
    aggregateId?: string,
    createdAt?: ModelStringKeyConditionInput,
    sortDirection?: ModelSortDirection,
    filter?: ModelEventFilterInput,
    limit?: number,
    nextToken?: string
  ): Promise<EventsByAggregateIdQuery> {
    const statement = `query EventsByAggregateId($aggregateId: ID, $createdAt: ModelStringKeyConditionInput, $sortDirection: ModelSortDirection, $filter: ModelEventFilterInput, $limit: Int, $nextToken: String) {
        eventsByAggregateId(aggregateId: $aggregateId, createdAt: $createdAt, sortDirection: $sortDirection, filter: $filter, limit: $limit, nextToken: $nextToken) {
          __typename
          items {
            __typename
            id
            aggregateId
            type
            sequenceNumber
            createdAt
            domainEvent
            updatedAt
            _version
            _deleted
            _lastChangedAt
          }
          nextToken
          startedAt
        }
      }`;
    const gqlAPIServiceArguments: any = {};
    if (aggregateId) {
      gqlAPIServiceArguments.aggregateId = aggregateId;
    }
    if (createdAt) {
      gqlAPIServiceArguments.createdAt = createdAt;
    }
    if (sortDirection) {
      gqlAPIServiceArguments.sortDirection = sortDirection;
    }
    if (filter) {
      gqlAPIServiceArguments.filter = filter;
    }
    if (limit) {
      gqlAPIServiceArguments.limit = limit;
    }
    if (nextToken) {
      gqlAPIServiceArguments.nextToken = nextToken;
    }
    const response = (await API.graphql(
      graphqlOperation(statement, gqlAPIServiceArguments)
    )) as any;
    return <EventsByAggregateIdQuery>response.data.eventsByAggregateId;
  }
  OnCreateEventListener: Observable<
    SubscriptionResponse<Pick<__SubscriptionContainer, "onCreateEvent">>
  > = API.graphql(
    graphqlOperation(
      `subscription OnCreateEvent {
        onCreateEvent {
          __typename
          id
          aggregateId
          type
          sequenceNumber
          createdAt
          domainEvent
          updatedAt
          _version
          _deleted
          _lastChangedAt
        }
      }`
    )
  ) as Observable<
    SubscriptionResponse<Pick<__SubscriptionContainer, "onCreateEvent">>
  >;

  OnUpdateEventListener: Observable<
    SubscriptionResponse<Pick<__SubscriptionContainer, "onUpdateEvent">>
  > = API.graphql(
    graphqlOperation(
      `subscription OnUpdateEvent {
        onUpdateEvent {
          __typename
          id
          aggregateId
          type
          sequenceNumber
          createdAt
          domainEvent
          updatedAt
          _version
          _deleted
          _lastChangedAt
        }
      }`
    )
  ) as Observable<
    SubscriptionResponse<Pick<__SubscriptionContainer, "onUpdateEvent">>
  >;

  OnDeleteEventListener: Observable<
    SubscriptionResponse<Pick<__SubscriptionContainer, "onDeleteEvent">>
  > = API.graphql(
    graphqlOperation(
      `subscription OnDeleteEvent {
        onDeleteEvent {
          __typename
          id
          aggregateId
          type
          sequenceNumber
          createdAt
          domainEvent
          updatedAt
          _version
          _deleted
          _lastChangedAt
        }
      }`
    )
  ) as Observable<
    SubscriptionResponse<Pick<__SubscriptionContainer, "onDeleteEvent">>
  >;
}
