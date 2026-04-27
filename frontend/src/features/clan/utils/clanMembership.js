export const findMyClan = (clans, userId) => {
  if (!Array.isArray(clans) || !userId) {
    return null;
  }

  return clans.find(
    (clan) => Array.isArray(clan.memberUserIds) && clan.memberUserIds.includes(userId),
  ) ?? null;
};
