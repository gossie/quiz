import { ModelInit, MutableModel, PersistentModelConstructor } from "@aws-amplify/datastore";





type EventMetaData = {
  readOnlyFields: 'updatedAt';
}

export declare class Event {
  readonly id: string;
  readonly aggregateId?: string;
  readonly type?: string;
  readonly sequenceNumber?: number;
  readonly createdAt?: string;
  readonly domainEvent?: string;
  readonly updatedAt?: string;
  constructor(init: ModelInit<Event, EventMetaData>);
  static copyOf(source: Event, mutator: (draft: MutableModel<Event, EventMetaData>) => MutableModel<Event, EventMetaData> | void): Event;
}