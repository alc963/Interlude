import { useState, useEffect } from 'react';
import { ApiBridge, JWT_LOCAL_STORAGE_KEY } from '../bridge/api_bridge';
import { local_storage_listen } from '../bridge/local_storage';

export default function useAuthenticated() {
  const [authenticated, set_authenticated] = useState<boolean>(() => {
    if (typeof window === 'undefined') return false;
    return ApiBridge.initialize();
  });

  useEffect(() => {
        local_storage_listen((key) => {
            if(key === JWT_LOCAL_STORAGE_KEY) {
                set_authenticated(ApiBridge.initialize());
            }
        });
  }, []);

  return { authenticated };
}