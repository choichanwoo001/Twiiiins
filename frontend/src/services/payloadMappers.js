const trimString = (value) => {
  if (value === undefined || value === null) {
    return undefined;
  }
  const trimmed = String(value).trim();
  return trimmed.length > 0 ? trimmed : undefined;
};

const sanitizeString = (value, { allowEmpty = false } = {}) => {
  if (value === undefined || value === null) {
    return undefined;
  }
  const trimmed = String(value).trim();
  if (trimmed.length === 0) {
    return allowEmpty ? '' : undefined;
  }
  return trimmed;
};

const sanitizeDate = (value) => sanitizeString(value);

const sanitizeTime = (value) => {
  const time = sanitizeString(value);
  if (!time) {
    return undefined;
  }
  const isValid = /^([01]\d|2[0-3]):[0-5]\d$/.test(time);
  return isValid ? time : undefined;
};

const sanitizeUrl = (value) => sanitizeString(value);

const sanitizeNumber = (value) => {
  if (value === undefined || value === null || value === '') {
    return undefined;
  }
  const num = Number(value);
  return Number.isFinite(num) ? num : undefined;
};

const sanitizeBoolean = (value, defaultValue = false) => {
  if (value === undefined || value === null) {
    return defaultValue;
  }
  return Boolean(value);
};

const sanitizeStringArray = (value) => {
  if (value === undefined || value === null) {
    return undefined;
  }
  if (!Array.isArray(value)) {
    return undefined;
  }
  return value
    .map((item) => sanitizeString(item))
    .filter((item) => item !== undefined);
};

const buildBaseNewsPayload = (data) => {
  const payload = {};
  const description = sanitizeString(data.description);
  const displayOrder = sanitizeNumber(data.displayOrder);
  const imageUrls = sanitizeStringArray(data.imageUrls);

  if (description !== undefined) {
    payload.description = description;
  }
  if (displayOrder !== undefined) {
    payload.displayOrder = displayOrder;
  }
  if (imageUrls !== undefined) {
    payload.imageUrls = imageUrls;
  }
  return payload;
};

const buildBaseMusicPayload = (data) => {
  const payload = {};
  const linkUrl = sanitizeUrl(data.linkUrl);
  const displayOrder = sanitizeNumber(data.displayOrder);

  if (linkUrl !== undefined) {
    payload.linkUrl = linkUrl;
  }

  if (displayOrder !== undefined) {
    payload.displayOrder = displayOrder;
  }
  return payload;
};

const buildBaseVideoPayload = (data) => {
  const payload = {};
  const displayOrder = sanitizeNumber(data.displayOrder);
  if (displayOrder !== undefined) {
    payload.displayOrder = displayOrder;
  }
  return payload;
};

const buildBaseEquipmentPayload = (data) => {
  const payload = {};
  const imageUrl = sanitizeUrl(data.imageUrl);
  const displayOrder = sanitizeNumber(data.displayOrder);
  if (imageUrl !== undefined) {
    payload.imageUrl = imageUrl;
  }
  if (displayOrder !== undefined) {
    payload.displayOrder = displayOrder;
  }
  return payload;
};

const buildBaseContactPayload = (data) => {
  const payload = {};
  const role = sanitizeString(data.role);
  const email = sanitizeString(data.email);
  const displayOrder = sanitizeNumber(data.displayOrder);
  if (role !== undefined) {
    payload.role = role;
  }
  if (email !== undefined) {
    payload.email = email;
  }
  if (displayOrder !== undefined) {
    payload.displayOrder = displayOrder;
  }
  return payload;
};

const buildBaseDownloadFilePayload = (data) => {
  const payload = {};
  const fileUrl = sanitizeUrl(data.fileUrl);
  const displayOrder = sanitizeNumber(data.displayOrder);
  if (fileUrl !== undefined) {
    payload.fileUrl = fileUrl;
  }
  if (displayOrder !== undefined) {
    payload.displayOrder = displayOrder;
  }
  return payload;
};

const buildBaseProjectPayload = (data) => {
  const payload = {};
  const subtitle = sanitizeString(data.subtitle);
  const location = sanitizeString(data.location);
  const director = sanitizeString(data.director);
  const thankYouText = sanitizeString(data.thankYouText);
  const moreInfoUrl = sanitizeUrl(data.moreInfoUrl);
  const descriptions = sanitizeStringArray(data.descriptions);
  const imageUrls = sanitizeStringArray(data.imageUrls);
  if (subtitle !== undefined) {
    payload.subtitle = subtitle;
  }
  if (location !== undefined) {
    payload.location = location;
  }
  if (director !== undefined) {
    payload.director = director;
  }
  if (thankYouText !== undefined) {
    payload.thankYouText = thankYouText;
  }
  if (moreInfoUrl !== undefined) {
    payload.moreInfoUrl = moreInfoUrl;
  }
  if (descriptions !== undefined) {
    payload.descriptions = descriptions;
  }
  if (imageUrls !== undefined) {
    payload.imageUrls = imageUrls;
  }
  if (Array.isArray(data.reviews)) {
    payload.reviews = data.reviews
      .map((review) => {
        const text = sanitizeString(review.text);
        const source = sanitizeString(review.source);
        if (!text) {
          return undefined;
        }
        return {
          text,
          ...(source ? { source } : {})
        };
      })
      .filter(Boolean);
  }
  return payload;
};

export const buildConcertCreatePayload = (data) => {
  const date = sanitizeDate(data.date);
  const location = sanitizeString(data.location);
  const name = sanitizeString(data.name);

  if (!date) {
    throw new Error('콘서트 날짜는 필수입니다.');
  }
  if (!location) {
    throw new Error('콘서트 장소는 필수입니다.');
  }
  if (!name) {
    throw new Error('콘서트 이름은 필수입니다.');
  }

  const payload = {
    date,
    location,
    name,
    isPast: sanitizeBoolean(data.isPast, false)
  };

  const startTime = sanitizeTime(data.startTime);
  const ticketInfo = sanitizeString(data.ticketInfo);
  const fullLocation = sanitizeString(data.fullLocation);
  const googleMapUrl = sanitizeUrl(data.googleMapUrl);
  const collaborationInfo = sanitizeString(data.collaborationInfo);

  if (startTime !== undefined) {
    payload.startTime = startTime;
  }
  if (ticketInfo !== undefined) {
    payload.ticketInfo = ticketInfo;
  }
  if (fullLocation !== undefined) {
    payload.fullLocation = fullLocation;
  }
  if (googleMapUrl !== undefined) {
    payload.googleMapUrl = googleMapUrl;
  }
  if (collaborationInfo !== undefined) {
    payload.collaborationInfo = collaborationInfo;
  }

  return payload;
};

export const buildConcertUpdatePayload = (data) => {
  const payload = {};
  const date = sanitizeDate(data.date);
  const location = sanitizeString(data.location);
  const name = sanitizeString(data.name);
  const startTime = sanitizeTime(data.startTime);
  const ticketInfo = sanitizeString(data.ticketInfo);
  const fullLocation = sanitizeString(data.fullLocation);
  const googleMapUrl = sanitizeUrl(data.googleMapUrl);
  const collaborationInfo = sanitizeString(data.collaborationInfo);

  if (date !== undefined) {
    payload.date = date;
  }
  if (location !== undefined) {
    payload.location = location;
  }
  if (name !== undefined) {
    payload.name = name;
  }
  if (startTime !== undefined) {
    payload.startTime = startTime;
  }
  if (ticketInfo !== undefined) {
    payload.ticketInfo = ticketInfo;
  }
  if (fullLocation !== undefined) {
    payload.fullLocation = fullLocation;
  }
  if (googleMapUrl !== undefined) {
    payload.googleMapUrl = googleMapUrl;
  }
  if (collaborationInfo !== undefined) {
    payload.collaborationInfo = collaborationInfo;
  }
  if (data.isPast !== undefined) {
    payload.isPast = Boolean(data.isPast);
  }
  return payload;
};

export const buildMusicCreatePayload = (data) => {
  const title = sanitizeString(data.title);
  const artist = sanitizeString(data.artist);
  const coverUrl = sanitizeUrl(data.coverUrl);

  if (!title) {
    throw new Error('음악 제목은 필수입니다.');
  }
  if (!artist) {
    throw new Error('음악 아티스트는 필수입니다.');
  }
  if (!coverUrl) {
    throw new Error('커버 이미지는 필수입니다.');
  }

  return {
    title,
    artist,
    coverUrl,
    ...buildBaseMusicPayload(data)
  };
};

export const buildMusicUpdatePayload = (data) => {
  const payload = buildBaseMusicPayload(data);
  const title = sanitizeString(data.title);
  const artist = sanitizeString(data.artist);
  const coverUrl = sanitizeUrl(data.coverUrl);

  if (title !== undefined) {
    payload.title = title;
  }
  if (artist !== undefined) {
    payload.artist = artist;
  }
  if (coverUrl !== undefined) {
    payload.coverUrl = coverUrl;
  }
  return payload;
};

export const buildVideoCreatePayload = (data) => {
  const title = sanitizeString(data.title);
  const embedUrl = sanitizeUrl(data.embedUrl);

  if (!title) {
    throw new Error('동영상 제목은 필수입니다.');
  }
  if (!embedUrl) {
    throw new Error('동영상 URL은 필수입니다.');
  }

  return {
    title,
    embedUrl,
    ...buildBaseVideoPayload(data)
  };
};

export const buildVideoUpdatePayload = (data) => {
  const payload = buildBaseVideoPayload(data);
  const title = sanitizeString(data.title);
  const embedUrl = sanitizeUrl(data.embedUrl);
  if (title !== undefined) {
    payload.title = title;
  }
  if (embedUrl !== undefined) {
    payload.embedUrl = embedUrl;
  }
  return payload;
};

export const buildNewsCreatePayload = (data) => {
  const date = sanitizeDate(data.date);
  const title = sanitizeString(data.title);

  if (!date) {
    throw new Error('뉴스 날짜는 필수입니다.');
  }
  if (!title) {
    throw new Error('뉴스 제목은 필수입니다.');
  }

  const payload = {
    date,
    title,
    ...buildBaseNewsPayload(data)
  };

  if (!payload.imageUrls) {
    payload.imageUrls = [];
  }

  return payload;
};

export const buildNewsUpdatePayload = (data) => {
  const payload = buildBaseNewsPayload(data);
  const date = sanitizeDate(data.date);
  const title = sanitizeString(data.title);
  if (date !== undefined) {
    payload.date = date;
  }
  if (title !== undefined) {
    payload.title = title;
  }
  return payload;
};

export const buildEquipmentCreatePayload = (data) => {
  const name = sanitizeString(data.name);
  if (!name) {
    throw new Error('장비 이름은 필수입니다.');
  }
  return {
    name,
    ...buildBaseEquipmentPayload(data)
  };
};

export const buildEquipmentUpdatePayload = (data) => {
  const payload = buildBaseEquipmentPayload(data);
  const name = sanitizeString(data.name);
  if (name !== undefined) {
    payload.name = name;
  }
  return payload;
};

export const buildPhotoGroupCreatePayload = (data) => {
  const title = sanitizeString(data.title);
  if (!title) {
    throw new Error('사진 그룹명은 필수입니다.');
  }
  const payload = { title };
  const displayOrder = sanitizeNumber(data.displayOrder);
  if (displayOrder !== undefined) {
    payload.displayOrder = displayOrder;
  }
  return payload;
};

export const buildPhotoGroupUpdatePayload = (data) => {
  const payload = {};
  const title = sanitizeString(data.title);
  const displayOrder = sanitizeNumber(data.displayOrder);
  if (title !== undefined) {
    payload.title = title;
  }
  if (displayOrder !== undefined) {
    payload.displayOrder = displayOrder;
  }
  return payload;
};

export const buildContactCreatePayload = (data) => {
  const name = sanitizeString(data.name);
  const email = sanitizeString(data.email);
  if (!name) {
    throw new Error('연락처 이름은 필수입니다.');
  }
  if (!email) {
    throw new Error('연락처 이메일은 필수입니다.');
  }
  return {
    name,
    ...buildBaseContactPayload({ ...data, email })
  };
};

export const buildContactUpdatePayload = (data) => {
  const payload = buildBaseContactPayload(data);
  const name = sanitizeString(data.name);
  if (name !== undefined) {
    payload.name = name;
  }
  return payload;
};

export const buildDownloadFileCreatePayload = (data) => {
  const name = sanitizeString(data.name);
  const fileUrl = sanitizeUrl(data.fileUrl);
  if (!name) {
    throw new Error('파일 이름은 필수입니다.');
  }
  if (!fileUrl) {
    throw new Error('파일 URL은 필수입니다.');
  }
  return {
    name,
    fileUrl,
    ...buildBaseDownloadFilePayload(data)
  };
};

export const buildDownloadFileUpdatePayload = (data) => {
  const payload = buildBaseDownloadFilePayload(data);
  const name = sanitizeString(data.name);
  if (name !== undefined) {
    payload.name = name;
  }
  return payload;
};

export const buildProjectCreatePayload = (data) => {
  const title = sanitizeString(data.title);
  const premiereDate = sanitizeDate(data.premiereDate);
  const urlSlug = sanitizeString(data.urlSlug || data.title);

  if (!title) {
    throw new Error('프로젝트 제목은 필수입니다.');
  }
  if (!premiereDate) {
    throw new Error('프로젝트 일시는 필수입니다.');
  }
  if (!urlSlug) {
    throw new Error('프로젝트 슬러그는 필수입니다.');
  }

  const payload = {
    title,
    premiereDate,
    urlSlug,
    ...buildBaseProjectPayload(data)
  };

  const coverImageUrl = sanitizeUrl(data.coverImageUrl);
  const displayOrder = sanitizeNumber(data.displayOrder);

  if (coverImageUrl !== undefined) {
    payload.coverImageUrl = coverImageUrl;
  }
  if (displayOrder !== undefined) {
    payload.displayOrder = displayOrder;
  }

  return payload;
};

export const buildProjectUpdatePayload = (data) => {
  const payload = buildBaseProjectPayload(data);
  const title = sanitizeString(data.title);
  const premiereDate = sanitizeDate(data.premiereDate);
  const urlSlug = sanitizeString(data.urlSlug);
  const coverImageUrl = sanitizeUrl(data.coverImageUrl);
  const displayOrder = sanitizeNumber(data.displayOrder);

  if (title !== undefined) {
    payload.title = title;
  }
  if (premiereDate !== undefined) {
    payload.premiereDate = premiereDate;
  }
  if (urlSlug !== undefined) {
    payload.urlSlug = urlSlug;
  }
  if (coverImageUrl !== undefined) {
    payload.coverImageUrl = coverImageUrl;
  }
  if (displayOrder !== undefined) {
    payload.displayOrder = displayOrder;
  }

  return payload;
};

export const buildProjectDetailPayload = (data) => buildProjectCreatePayload(data);

export const sanitizeQueryParams = (params) => {
  const sanitized = {};
  Object.entries(params || {}).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      const cleaned = value.map((item) => trimString(item)).filter(Boolean);
      if (cleaned.length > 0) {
        sanitized[key] = cleaned;
      }
    } else {
      const cleaned = trimString(value);
      if (cleaned !== undefined) {
        sanitized[key] = cleaned;
      }
    }
  });
  return sanitized;
};


