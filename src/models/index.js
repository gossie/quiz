// @ts-check
import { initSchema } from '@aws-amplify/datastore';
import { schema } from './schema';



const { Event } = initSchema(schema);

export {
  Event
};